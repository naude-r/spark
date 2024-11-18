/*
 * Copyright 2011- Per Wendel
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spark.route;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import spark.utils.SparkUtils;
import spark.utils.StringUtils;

/**
 * Class that holds information about routes
 *
 * @author Per Wendel
 */
class RouteEntry {

    private static final Logger LOG = LoggerFactory.getLogger(RouteEntry.class);

    HttpMethod httpMethod;
    String path;
    String acceptedType;
    Object target;

    RouteEntry() {
    }

    RouteEntry(RouteEntry entry) {
        this.httpMethod = entry.httpMethod;
        this.path = entry.path;
        this.acceptedType = entry.acceptedType;
        this.target = entry.target;
    }

    boolean matches(HttpMethod httpMethod, String path) {
        if ((httpMethod == HttpMethod.before || httpMethod == HttpMethod.after || httpMethod == HttpMethod.afterafter)
                && (this.httpMethod == httpMethod)
                && this.path.equals(SparkUtils.ALL_PATHS)) {
            // Is filter and matches all

            return true;
        }
        boolean match = false;
        if (this.httpMethod == httpMethod) {
            match = matchPath(path);
        }
        return match;
    }

    //CS304 Issue link: https://github.com/perwendel/spark/issues/1151
    private boolean matchPath(String input) { // NOSONAR
        if (!this.path.endsWith("*") && this.path.equals(input)) {
            // Paths are the same
            return true;
        }
        // Regex expressions should start with '~/'  (end '/' is optional)
        if(this.path.startsWith("~/")) {
            String routePath = StringUtils.cleanRegex(this.path);
            Pattern pattern = Pattern.compile(routePath, Pattern.CASE_INSENSITIVE);
            return pattern.matcher(input).find();
        }
        // Match slashes (return false if they don't match except when it is optional)
        if (!this.path.endsWith("*")
            // If the user input has a slash on the end, either our path should end in slash or optional
            && ((input.endsWith("/") && !(this.path.endsWith("/") || this.path.endsWith("/?"))) // NOSONAR
            // If we specified that the path must finish with a slash, user input must as well
            || (!input.endsWith("/") && this.path.endsWith("/")))) {
            // One and not both ends with slash
            return false;
        }

        // check params
        List<String> thisPathList = SparkUtils.convertRouteToList(this.path);
        List<String> pathList = SparkUtils.convertRouteToList(input);
        // Remove optional "/?" when using params
        if(thisPathList.indexOf("?") == thisPathList.size() - 1) {
            thisPathList.remove("?");
        }

        int thisPathSize = thisPathList.size();
        int pathSize = pathList.size();

        if (thisPathSize == pathSize) {
            for (int i = 0; i < thisPathSize; i++) {
                String thisPathPart = thisPathList.get(i);
                String pathPart = pathList.get(i);

                if ((i == thisPathSize - 1) && (thisPathPart.equals("*") && this.path.endsWith("*"))) {
                    // wildcard match
                    return true;
                }

                if ((!thisPathPart.startsWith(":"))
                        && !thisPathPart.equals(pathPart)
                        && !thisPathPart.equals("*")) {
                    return false;
                }
            }
            // All parts matched
            return true;
        } else {
            // Number of "path parts" not the same
            // check wild card:
            if (this.path.endsWith("*")) {
                if (pathSize == (thisPathSize - 1) && (input.endsWith("/"))) {
                    // Hack for making wildcards work with trailing slash
                    pathList.add("");
                    pathList.add("");
                    pathSize += 2;
                }

                if (thisPathSize < pathSize) {
                    for (int i = 0; i < thisPathSize; i++) {
                        String thisPathPart = thisPathList.get(i);
                        String pathPart = pathList.get(i);
                        if (thisPathPart.equals("*") && (i == thisPathSize - 1) && this.path.endsWith("*")) {
                            // wildcard match
                            return true;
                        }
                        if (!thisPathPart.startsWith(":")
                                && !thisPathPart.equals(pathPart)
                                && !thisPathPart.equals("*")) {
                            return false;
                        }
                    }
                    // All parts matched
                    return true;
                }
                // End check wild card
            }
            if (thisPathSize > pathSize) {
                for (int i = pathSize - 1; i > -1; i--) {
                    if(!thisPathList.get(i).equals(pathList.get(i))){
                        return false;
                    }
                }
                for (int i = pathSize; i < thisPathSize; i++) {
                    if (!thisPathList.get(i).endsWith("?")) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    }

    @Override
    public String toString() {
        return httpMethod.name() + ", " + path + ", " + target;
    }
}
