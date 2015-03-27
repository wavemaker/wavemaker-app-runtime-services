/*global WM, wm, window, document, navigator, Image, location, console*/
/*jslint todo: true */

/**
 * @ngdoc service
 * @name wm.utils.Utils
 * @requires $rootScope
 * @requires $location
 * @requires $window
 * @requires CONSTANTS
 * @requires $sce
 * @description
 * The `Utils` service provides utility methods.
 */

WM.module('wm.utils', [])
    .service("Utils", ['$rootScope', '$location', '$window', 'CONSTANTS', '$sce', function ($rootScope, $location, $window, APPCONSTANTS, $sce) {
        "use strict";

        var userAgent = navigator.userAgent,
            scriptEl = document.createElement('script'),
            linkEl = document.createElement('link'),
            headNode = document.getElementsByTagName("head")[0],
            isAppleProduct = /Mac|iPod|iPhone|iPad/.test(navigator.platform),
            sliceFn = Array.prototype.slice,
            REGEX = {
                SNAKE_CASE: /[A-Z]/g,
                ANDROID: /Android/i,
                IPHONE: /iPhone/i,
                IPOD: /iPod/i,
                MOBILE: /Mobile/i,
                WINDOWS: /Windows Phone/i,
                SUPPORTED_IMAGE_FORMAT: /\.(bmp|gif|jpe|jpg|jpeg|tif|tiff|pbm|png|ico)$/i,
                SUPPORTED_FILE_FORMAT: /\.(txt|js|css|html|script|properties|json|java|xml|smd|xmi|sql|log)$/i,
                SUPPORTED_AUDIO_FORMAT: /\.(mp3|ogg|webm|wma|3gp|wav)$/i,
                SUPPORTED_VIDEO_FORMAT: /\.(mp4|ogg|webm|wmv|mpeg|mpg|avi)$/i,
                PAGE_RESOURCE_PATH: /^\/pages\/.*\.(js|css|html|json)$/,
                MIN_PAGE_RESOURCE_PATH: /.*(page.min.html)$/,
                VALID_EMAIL: /^[a-zA-Z][\w.]+@[a-zA-Z_]+?\.[a-zA-Z.]{1,4}[a-zA-Z]$/,
                VALID_WEB_URL: /^(http[s]?:\/\/)(www\.){0,1}[a-zA-Z0-9\.\-]+\.[a-zA-Z]{2,5}[\.]{0,1}/,  //ref : http://stackoverflow.com/questions/4314741/url-regex-validation
                REPLACE_PATTERN: /\$\{([^\}]+)\}/g,
                ZIP_FILE: /\.zip$/i,
                EXE_FILE: /\.exe$/i
            },
            NUMBER_TYPES = ["int", "integer", "float", "double", "short", "byte"],
            SYSTEM_FOLDER_PATHS = {
                'project': ['../lib', '../project', '../project/services', '../project/lib', '../project/src', '../project/test', '../project/src/main', '../project/src/main/webapp', '../project/src/main/resources', '../project/src/main/webapp/services', '../project/src/main/webapp/resources', '../project/src/main/webapp/pages', '../project/src/main/webapp/resources/images', '../project/src/main/webapp/resources/WEB-INF', '../project/src/main/webapp/resources/ngLocale', '../project/src/main/webapp/resources/i18n', '../project/src/main/webapp/resources/images/imagelists', '../project/src/main/webapp/resources/audio', '../project/src/main/webapp/resources/video'],
                'resources': ['', '/services', '/resources', '/WEB-INF', '/app', '/pages', '/resources/i18n', '/resources/ngLocale', 'resources/images', 'resources/audio', 'resources/video', 'resources/images/imagelists'],
                'lib': ['../lib'],
                'jar': ['../lib'],
                'locale': ['resources/i18n'],
                'services': ['../services'],
                'image': ['resources/images', 'resources/images/imagelists'],
                'audio': ['resources/audio'],
                'video': ['resources/video']
            },
            hasLocalStorage = 'localStorage' in window && window.localStorage !== null,
            browserStorage = {
                storeItem: function (key, value) {
                    if (hasLocalStorage) {
                        window.localStorage.setItem(key, value);
                    }
                },
                getItem: function (key) {
                    if (hasLocalStorage) {
                        return window.localStorage.getItem(key);
                    }
                },
                deleteItem: function (key) {
                    if (hasLocalStorage) {
                        delete window.localStorage[key];
                    }
                }
            },
            isIE = function () {
                return userAgent.indexOf('MSIE') > -1;
            },
            isIE11 = function () {
                return navigator.appVersion.indexOf('Trident/') > -1;
            },
            getNode = function (tree, nodeId) {
                var index, treeLength;
                /*Return undefined if the "tree" is undefined*/
                if (WM.isUndefined(tree)) {
                    return undefined;
                }
                treeLength = tree.length;
                /*Loop through the tree and match the specified nodeId against the ids of the tree elements*/
                for (index = 0; index < treeLength; index += 1) {
                    /*Return the node if it exists*/
                    if (tree[index].id === nodeId) {
                        return tree[index];
                    }
                }
                /*Return undefined if the tree does not contain the node*/
                return undefined;
            },
            variableCategoryMap = {
                "wm.Variable": "variable",
                "wm.ServiceVariable": "service-variable",
                "wm.LiveVariable": "live-variable",
                "wm.LoginVariable": "login-variable",
                "wm.LogoutVariable": "logout-variable",
                "wm.NavigationVariable": "navigation-variable",
                "wm.NotificationVariable": "notification-variable",
                "wm.TimerVariable": "time"
            },
            fieldTypeWidgetTypeMap = {
                'integer': ['Number', 'Text', 'Slider'],
                'big_integer': ['Number', 'Text', 'Slider'],
                'short': ['Number', 'Text', 'Slider'],
                'byte': ['Number', 'Text', 'Slider'],
                'date': ['Date', 'Text'],
                'boolean': ['Checkbox', 'Text'],
                'list': ['Select', 'Text', 'Datalist'],
                'float': ['Number', 'Text', 'Slider'],
                'big_decimal': ['Number', 'Text', 'Slider'],
                'double': ['Number', 'Text', 'Slider'],
                'string': ['Text', 'Textarea', 'Password', 'RichText'],
                'character': ['Text', 'Textarea', 'RichText'],
                'text': ['Textarea', 'Text', 'RichText'],
                'clob': ['Textarea', 'Text', 'RichText'],
                'blob': ['Upload', 'Textarea', 'Text', 'RichText'],
                'time': ['Time', 'Text'],
                'timestamp': ['Timestamp', 'Text', 'Date', 'Time'],
                'custom': ['Text', 'Textarea', 'Password', 'RichText', 'Checkbox', 'Number', 'Slider']
            },
            indexPage = getIndexPage();

        /* set default attrs for link */
        linkEl.rel = "stylesheet";
        linkEl.type = "text/css";

        /* convert camelCase string to a snake-case string */
        function hyphenate(name) {
            return name.replace(REGEX.SNAKE_CASE, function (letter, pos) {
                return (pos ? '-' : '') + letter.toLowerCase();
            });
        }
        /* convert a hyphenated string to a space separated string. */
        function deHyphenate(name) {
            return name.split('-').join(' ');
        }
        /* convert camelCase string to a space separated string */
        function spaceSeparate(name) {
            if (name === name.toUpperCase()) {
                return name;
            }
            return name.replace(REGEX.SNAKE_CASE, function (letter, pos) {
                return (pos ? ' ' : '') + letter;
            });
        }

        /*Replace the character at a particular index*/
        function replaceAt(string, index, character) {
            return string.substr(0, index) + character + string.substr(index + character.length);
        }

        /*Replace '.' with space and capitalize the next letter*/
        function periodSeparate(name) {
            var dotIndex;
            dotIndex = name.indexOf('.');
            if (dotIndex !== -1) {
                name = replaceAt(name, dotIndex + 1, name.charAt(dotIndex + 1).toUpperCase());
                name = replaceAt(name, dotIndex, ' ');
            }
            return name;
        }

        /* capitalize the first-letter of the string passed */
        function initCaps(name) {
            if (!name) {
                return '';
            }
            return name.charAt(0).toUpperCase() + name.substring(1);
        }

        function prettifyLabel(label) {
            /*capitalize the initial Letter*/
            label = initCaps(label);
            /*Convert camel case words to separated words*/
            label = spaceSeparate(label);
            /*Replace '.' with space and capitalize the next letter*/
            label = periodSeparate(label);
            return label;
        }

        function isValidWebURL(url) {
            return (REGEX.VALID_WEB_URL).test(url);
        }

        /* returns the zIndex of the parent elements overlay */
        function getParentOverlayElZIndex(element) {
            var parentEl, parentOverlay, parentOvleryZIndex;
            parentEl = element.parent().closest('[widgetid]');
            /* fetch the z-index of parent element */
            if (parentEl.length > 0) {
                parentEl = parentEl.eq(0);
                parentOverlay = parentEl.data('overlayElement');
                if (parentOverlay) {
                    parentOvleryZIndex = parentOverlay.css('zIndex');
                } else if (parentEl.parent().data('overlayElement')) {
                    parentOvleryZIndex = parseInt(parentEl.parent().data('overlayElement').css('zIndex'), 10) + 10;
                } else {
                    /*Temporary fix to assign z-index for elements with no parent overlayElement - Ex: Database-column*/
                    parentOvleryZIndex = 10;
                }
            } else {
                return 100;
            }
            return +parentOvleryZIndex;
        }

        function formatVariableIconClass(variableCategory) {
            return variableCategoryMap[variableCategory];
        }

        /*helper function for prepareFieldDefs*/
        function pushFieldDef(dataObject, columnDefObj, namePrefix, propObj, noModifyTitle) {
            /*loop over the fields in the dataObject to process them*/
            var modifiedTitle;
            WM.forEach(dataObject, function (value, title) {
                if (noModifyTitle) {
                    modifiedTitle = title;
                } else {
                    /*capitalize the initial Letter*/
                    modifiedTitle = initCaps(title);
                    /*Convert camel case words to separated words*/
                    modifiedTitle = spaceSeparate(modifiedTitle);
                    /*Replace '.' with space and capitalize the next letter*/
                    modifiedTitle = periodSeparate(modifiedTitle);
                    modifiedTitle = deHyphenate(modifiedTitle);
                    modifiedTitle = namePrefix ? initCaps(namePrefix) + " " + modifiedTitle : modifiedTitle;
                }
                title = namePrefix ? namePrefix + "." + title : title;
                var defObj = propObj.setBindingField ? {'displayName': modifiedTitle, 'field': title} : {'displayName': modifiedTitle};
                /*if field is a leaf node, push it in the columnDefs*/
                if (!WM.isObject(value) || (WM.isArray(value) && !value[0])) {
                    /*if the column counter has reached upperBound return*/
                    if (propObj.columnCount === propObj.upperBound) {
                        return;
                    }
                    columnDefObj.push(defObj);

                    /*increment the column counter*/
                    propObj.columnCount += 1;
                } else {
                    /*else field is an object, process it recursively*/
                    /* if parent node to be included, include it */
                    if (propObj.includeParentNode && propObj.columnCount !== propObj.upperBound) {
                        columnDefObj.push(defObj);
                    }

                    /* if field is an array node, process its first child */
                    if (WM.isArray(value) && value[0]) {
                        pushFieldDef(value[0], columnDefObj, title + "[0]", propObj);
                    } else {
                        pushFieldDef(value, columnDefObj, title, propObj);
                    }
                }
            });
        }

        /*function to prepare column definition objects from the data provided*/
        function prepareFieldDefs(data, columnUpperBound, includeParentNode, noModifyTitle) {
            var defaultDefs = false,
                dataObject,
                columnDef = [];
            /*if no data provided, initialize default column definitions*/
            if (!data || data.length === 0) {
                data = [
                    {"column1": '', "column2": '', "column3": '', "column4": '', "column5": ''}
                ];
                defaultDefs = true;
            }

            dataObject = WM.isArray(data) ? data[0] : [data];
            /*first of the many data objects from grid data*/
            pushFieldDef(dataObject, columnDef, '', {'upperBound': columnUpperBound, 'columnCount': 0, includeParentNode: includeParentNode, setBindingField: !defaultDefs}, noModifyTitle);
            return columnDef;
        }

        /*function to swap two elements in an array*/
        function swapArrayElements(array, index1, index2) {
            var temp = array[index1];
            array[index1] = array[index2];
            array[index2] = temp;
            return array;
        }

        /*function to swap the given two properties in the object*/
        function swapProperties(data, property1, property2) {
            var temp = data[property1];
            data[property1] = data[property2];
            data[property2] = temp;
        }


        /*function to check if fn is a function and then execute*/
        function triggerFn(fn) {
            if (WM.isFunction(fn)) {
                fn.apply(null, sliceFn.call(arguments, 1));
            }
        }

        /*function to check if the stylesheet is already loaded */
        function isStyleSheetLoaded(href) {
            return WM.element("link[href='" + href + "']").length > 0;
        }

        /*function to remove stylesheet if the stylesheet is already loaded */
        function removeStyleSheetLoaded(href) {
            var styleTag = WM.element("link[href='" + href + "']");
            if (styleTag.length) {
                styleTag.remove();
            }
        }

        /*function to load a stylesheet */
        function loadStyleSheet(url, attr) {
            if (isStyleSheetLoaded(url)) {
                return;
            }
            var link = linkEl.cloneNode();
            link.href = url;
            /*To add attributes to link tag*/
            if (attr && attr.name) {
                link.setAttribute(attr.name, attr.value);
            }
            headNode.appendChild(link);
        }

        /*function to load stylesheets */
        function loadStyleSheets(urlArray) {
            if (!urlArray) {
                return;
            }
            /* if the first argument is not an array, convert it to an array */
            if (!WM.isArray(urlArray)) {
                urlArray = [urlArray];
            }
            WM.forEach(urlArray, loadStyleSheet);
        }

        /*function to check if the script is already loaded*/
        function isScriptLoaded(src) {
            return WM.element("script[src='" + src + "']").length > 0 || WM.element("script[data-src='" + src + "']").length > 0;
        }

        /* util function to load the content from a url */
        function fetchContent(dataType, url, successCallback, errorCallback, inSync) {

            // IE9: xdomain.js will not allow us to make synchronous requests. Use nativeXMLHTTP
            if (inSync && window.nativeXMLHTTP) {
                var xhr = new window.nativeXMLHTTP(),
                    response;
                xhr.open('GET', url, false);
                xhr.onload = function () {
                    if (xhr.readyState === 4) {
                        if (xhr.status === 200) {
                            response = xhr.responseText;
                            if (dataType === 'json') {
                                response = JSON.parse(xhr.responseText);
                            }
                            successCallback(response);
                        } else {
                            errorCallback();
                        }
                    }
                };
                xhr.send();
            } else {
                WM.element.ajax({
                    type: 'get',
                    dataType: dataType,
                    url: url,
                    success: successCallback,
                    error: errorCallback,
                    async: !inSync
                });
            }
        }

        /*function to load a javascript files */
        function loadScripts(scripts, onsuccess, onerror, hasError, jqxhr) {

            if (!scripts || scripts.length === 0) {
                if (hasError) {
                    triggerFn(onerror, jqxhr);
                } else {
                    triggerFn(onsuccess, jqxhr);
                }
                return;
            }

            var url = scripts.shift();
            if (isScriptLoaded(url)) {
                loadScripts(scripts, onsuccess, onerror);
                return;
            }

            WM.element.getScript(url)
                .done(function (script, textStatus, jqxhr) {
                    loadScripts(scripts, onsuccess, onerror, false, jqxhr);
                })
                .fail(function (jqxhr) {
                    loadScripts([], onsuccess, onerror, true, jqxhr);
                });
        }

        function loadScriptsInSync(urlArray) {

            if (urlArray.length === 0) {
                return;
            }

            var url = urlArray.shift();

            if (isScriptLoaded(url) || !url) {
                return;
            }

            if (url) {
                fetchContent('text', url, function (response) {
                    var script = scriptEl.cloneNode();
                    script.text = response;
                    script.setAttribute('data-src', url);
                    headNode.appendChild(script);
                    loadScriptsInSync(urlArray);
                }, null, true); //load in sync
            }
        }

        /* functions for resource Tab*/
        function isImageFile(fileName) {
            return (REGEX.SUPPORTED_IMAGE_FORMAT).test(fileName);
        }

        function isZipFile(fileName) {
            return (REGEX.ZIP_FILE).test(fileName);
        }

        function isExeFile(fileName) {
            return (REGEX.EXE_FILE).test(fileName);
        }

        function isTextLikeFile(fileName) {
            return (REGEX.SUPPORTED_FILE_FORMAT).test(fileName);
        }

        function isAudioFile(fileName) {
            return (REGEX.SUPPORTED_AUDIO_FORMAT).test(fileName);
        }

        function isVideoFile(fileName) {
            return (REGEX.SUPPORTED_VIDEO_FORMAT).test(fileName);
        }

        function isPageResource(path) {
            return (REGEX.PAGE_RESOURCE_PATH).test(path) && !(REGEX.MIN_PAGE_RESOURCE_PATH).test(path);
        }

        function isAndroid() {
            return (REGEX.ANDROID.test(userAgent));
        }

        function isAndroidPhone() {
            return isAndroid() && (REGEX.MOBILE.test(userAgent));
        }

        function isIphone() {
            return (REGEX.IPHONE.test(userAgent));
        }

        function isIpod() {
            return (REGEX.IPOD.test(userAgent));
        }

        function isWindowsPhone() {
            return (REGEX.WINDOWS.test(userAgent));
        }

        function isMobile() {
            if (APPCONSTANTS.isRunMode) {
                return isAndroidPhone() || isIphone() || isIpod() || isWindowsPhone() || WM.element('#wm-mobile-display:visible').length > 0;
            }

            return false;
        }

        /*function to check valid java package name*/
        function isValidJavaPackageName(pkgName) {
            return pkgName.match(/^\w[\w\d_.]*[\w\d]$/);
        }

        /*This function returns the url to the image after checking the validity of url*/
        function getImageUrl(urlString) {
            if (APPCONSTANTS.isRunMode) {
                return urlString;
            }
            /*In studio mode before setting picturesource, check if the studioController is loaded and new picturesource is in "styles/images/" path or not.
             * When page is refreshed, loader.gif will be loaded first and it will be in "style/images/".
             * Prepend "../services/projects/" + $rootScope.project.id + "/web/resources/images/imagelists/"  if the image url is just image name in the project root,
             * and if the url pointing to resources/images/ then "../services/projects/" + $rootScope.project.id + "/web/"*/
            if (isValidWebURL(urlString)) {
                return urlString;
            }
            if (!isImageFile(urlString)) {
                urlString = "resources/images/imagelists/default-image.png";
            }

            // if the resource to be loaded is inside a prefab
            if (stringStartsWith(urlString, "../services/prefabs")) {
                return urlString;
            }

            urlString = "../services/projects/" + $rootScope.project.id + "/resources/web/" + urlString;
            return urlString;
        }

        /*This function returns the url to the resource after checking the validity of url*/
        function getResourceURL(urlString) {
            if (isValidWebURL(urlString)) {
                return $sce.trustAsResourceUrl(urlString);
            }
            if (APPCONSTANTS.isRunMode) {
                return urlString;
            }

            // if the resource to be loaded is inside a prefab
            if (stringStartsWith(urlString, "../services/prefabs")) {
                return urlString;
            }

            urlString = "../services/projects/" + $rootScope.project.id + "/resources/web/" + urlString;
            return urlString;
        }

        /*This function returns the url to the backgroundImage*/
        function getBackGroundImageUrl(urlString) {
            if (urlString === "" || urlString === "none") {
                return urlString;
            }
            return "url(" + getImageUrl(urlString) + ")";
        }

        /* defining safe apply on root scope, so that it is accessible to all other $scope variables.*/
        $rootScope.$safeApply = function ($scope, fn) {
            if ($rootScope.$$phase || $scope.$$phase) {
                //don't worry, the value gets set and AngularJS picks up on it...
                triggerFn(fn);
            } else {
                //this will fire to tell angularjs to notice that a change has happened
                //if it is outside of it's own behavior...
                $scope.$apply(fn);
            }
        };

        /* util function which returns true if the given string starts with test string else returns false */
        function stringStartsWith(str, startsWith, ignoreCase) {
            if (!str) {
                return false;
            }

            var regEx = new RegExp('^' + startsWith, ignoreCase ? "i" : []);

            return regEx.test(str);
        }

        /* util function which returns true if the given string starts with test string else returns false */
        function stringEndsWith(str, endsWith, ignoreCase) {
            if (!str) {
                return false;
            }

            var regEx = new RegExp(endsWith + '$', ignoreCase ? "i" : []);

            return regEx.test(str);
        }

        /* function to check if provided object is empty*/
        function isEmptyObject(obj) {
            if (WM.isObject(obj) && !WM.isArray(obj)) {
                return Object.keys(obj).length === 0;
            }
            return false;
        }

        /* function to verify whether the email is valid or not*/
        function isValidEmail(email) {
            return REGEX.VALID_EMAIL.test(email);
        }

        /* fetch the column names and nested column names from the propertiesMap object */
        function fetchPropertiesMapColumns(propertiesMap, namePrefix) {
            var columns = {}, relatedColumnsArr = [];
            /* iterated trough the propertiesMap columns of all levels and build object with columns having required configuration*/
            WM.forEach(propertiesMap.columns, function (val) {
                /* if the object is nested type repeat the above process for that nested object through recursively */
                if (val.isRelated) {
                    if (val.isList) {
                        return;
                    }
                    relatedColumnsArr.push(val);
                } else {
                    /* otherwise build object with required configuration */
                    var columnName = namePrefix ? namePrefix + "." + val.fieldName : val.fieldName;
                    columns[columnName] = {};
                    columns[columnName].type = val.type;
                    columns[columnName].isPrimaryKey = val.isPrimaryKey;
                    columns[columnName].generator = val.generator;
                }
            });
            WM.forEach(relatedColumnsArr, function (val) {
                WM.extend(columns, fetchPropertiesMapColumns(val, val.fieldName));
            });

            return columns;
        }

        /*set empty values to all properties of  given object */
        function resetObjectWithEmptyValues(object) {
            var properties;
            if (object) {
                properties = {};
                WM.forEach(object, function (value, key) {
                    properties[key] = '';
                });
            }
            return properties;
        }

        /*function get current Page*/
        function getCurrentPage() {
            var pathName = location.pathname;
            return pathName.split("/").pop() || 'index.html';
        }

        /*function to find if executing in debug mode or normal mode*/
        function isDebugMode() {
            return getCurrentPage().indexOf('-debug') !== -1;
        }

        /*function get current Page*/
        function getIndexPage() {
            return isDebugMode() ? 'index-debug.html' : 'index.html';
        }

        /*function to go to a route in index page*/
        function redirectToIndexPage(path) {
            if (!path) {
                $window.location = indexPage;
            } else {
                $window.location = indexPage + '#' + path;
            }
        }

        /*function to go to a route in index page*/
        function redirectToLoginPage() {
            var locationHash = $location.$$path,
                currentPageName = getCurrentPage();

            /* store the reference url in local storage*/
            browserStorage.storeItem('wm.referenceUrlToStudio', currentPageName + '#' + locationHash);

            $window.location = APPCONSTANTS.LOGIN_PAGE;
        }

        /*function to handle the load event of the iframe which is called when the iframe is loaded after form submit to
         * handle manipulations after file upload.*/
        function handleUploadIFrameOnLoad(iFrameElement, successCallback, errorCallback) {
            var serverResponse;
            /*removing event listener for the iframe*/
            iFrameElement.off("load", function () {
                handleUploadIFrameOnLoad(iFrameElement, successCallback, errorCallback);
            });
            /*obtaining the server response of the form submit and prefab import*/
            serverResponse = WM.element('#fileUploadIFrame').contents().find('body').text();

            /*removing the iframe element from the DOM markup after import/upload is successful.*/
            iFrameElement.first().remove();
            /*triggering the callback function when succes response is encountered*/
            if (!serverResponse.errorDetails) {
                triggerFn(successCallback, serverResponse);
            } else {
                triggerFn(errorCallback);
            }
        }

        /*TODO: file upload fallback has been implemented currently only for prefabs. Need to implement for other scenarios.*/
        /*function to facilitate file upload fallback when HTML5 File API is not supported by the browser*/
        function fileUploadFallback(uploadConfig, successCallback, errorCallback) {
            /*creating a hidden iframe to facilitate file upload by way of form submit.*/
            var iFrameElement = WM.element("<iframe id='fileUploadIFrame' name='fileUploadIFrame' style='display: none;'></iframe>"),
                formElement,
                formAction,
                dataArray,
                index,
                dataObj,
                dataElement,
                elementName,
                elementValue,
                key;

            if (uploadConfig) {
                formAction = uploadConfig.url;
            }

            /*appending iframe element to the body*/
            WM.element('body').append(iFrameElement);

            /*event handler for handling load event of iframe*/
            iFrameElement.on("load", function () {
                handleUploadIFrameOnLoad(iFrameElement, successCallback, errorCallback);
            });

            formElement = WM.element("*[name=" + uploadConfig.formName + "]").first();

            /*creating fields which are necessary as params for importing resources.*/
            if (uploadConfig.data) {
                dataArray = uploadConfig.data;
                /*iterating over data array*/
                for (index = 0; index < dataArray.length; index += 1) {
                    dataObj = dataArray[index];
                    /*iterating over each object to get element name and element value*/
                    for (key in dataObj) {
                        if (dataObj.hasOwnProperty(key)) {
                            elementName = key;
                            elementValue = dataObj[key];
                        }
                    }
                    /*creating element using element name and value obtained.*/
                    dataElement = WM.element('<input type="hidden" name="' + elementName + '" value="' + elementValue + '"/>');
                    /*appending element to formElement*/
                    formElement.append(dataElement);
                }
            }

            /*setting form attributes before form submit. Applying iframe as form target to avoid page reflow.*/
            formElement.attr({
                "target": "fileUploadIFrame",
                "action": formAction,
                "method": "post",
                "enctype": "multipart/form-data",
                "encoding": "multipart/form-data"
            });

            /*triggering the form submit event*/
            formElement.submit();
        }

        /*
         * Util method to find the value of a key in the object
         * Examples:
         * var a = {
         *  b: {
         *      c : {
         *          d: "test"
         *      }
         * }
         * Utils.findValue(a, "b.c.d") --> "test"
         * Utils.findValue(a, "b.c") --> {d: "test"}
         * Utils.findValue(a, "e") --> undefined
         */
        function findValueOf(obj, key) {
            if (!obj || !key) {
                return;
            }

            var parts = key.split(".");

            /* iterate through the parts and find the value of obj[key] */
            while (parts.length !== 0 && obj) {
                obj = obj[parts.shift()];
            }

            return obj;
        }

        /*
         * Util method to replace patterns in string with object keys or array values
         * Examples:
         * Utils.replace("Hello, ${first} ${last} !", {first: "wavemaker", last: "ng"}) --> Hello, wavemaker ng
         * Utils.replace("Hello, ${0} ${1} !", ["wavemaker","ng"]) --> Hello, wavemaker ng
         */
        function replace(template, map) {
            if (!template) {
                return;
            }

            return template.replace(REGEX.REPLACE_PATTERN, function (match, key) {
                return findValueOf(map, key);
            });
        }

        /* returns the prefab names loaded in the markup of current page */
        function getLoadedPrefabNames() {
            return WM.element("[prefabname]").map(function (i, el) {return WM.element(el).attr("prefabname"); });
        }

        function initializeAction(op, callback) {
            var img = new Image(),
                version = ($rootScope.studioInfo && $rootScope.studioInfo.product && $rootScope.studioInfo.product.version) || '',
                revision = ($rootScope.studioInfo && $rootScope.studioInfo.product && $rootScope.studioInfo.product.revision) || '';
            img.onload = function () {
                /* image loaded successfully. trigger the callback */
                triggerFn(callback);
            };
            img.onerror = function () {
                /* image failed to load. trigger the callback */
                triggerFn(callback);
            };

            img.src = "http://wavemaker.com/img/blank.gif?op=" + op + "&v=" + version + "&r=" + revision + "&preventCache=" + String(Math.random(new Date().getTime())).replace(/\D/, "").substring(0, 8);
        }

        function getNodeFromJson(tree, nodeId, parentNodeId) {
            var nodeUId,
                parentNodeUId,
                nodeIndex,
                nodeStartIndex,
                node;

            /*Loop through the nodeMap of the tree.*/
            WM.element.each(tree[0].nodeMap, function (uid, name) {
                /*Check if the name matches with the specified nodeId*/
                if (name === nodeId) {
                    nodeUId = uid;

                    if (!parentNodeId) {
                        return false;
                    }
                    /*Check if the name of the parent node matches with the parentNodeId.
                    * If matched, break out of the loop.*/
                    parentNodeUId = nodeUId.substr(0, nodeUId.lastIndexOf("-"));
                    if (tree[0].nodeMap[parentNodeUId] === parentNodeId) {
                        return false;
                    }
                }
            });

            /*Check for sanity.*/
            if (nodeUId) {
                nodeIndex = nodeUId.split("-");
                nodeStartIndex = nodeIndex.shift();
                node = tree[nodeStartIndex];

                /*Get the node based on the nodeIndex*/
                nodeIndex.forEach(function (index) {
                    node = node.children[index];
                });
                return node;
            }
        }

        function addNodeToJson(tree, nodeId, parentNodeUId, options, parentNodeId) {
            var node = {
                    "id": nodeId,
                    "collapsed": options && options.collapsed,
                    "class": options && options.class,
                    "active": options && options.active,
                    "props": options && options.nodeProps,
                    "isDeletable": options && options.isDeletable,
                    "onDelete": options && options.onDelete
                },
                parentIndex,
                parentStartIndex,
                parentNode,
                existingNode;

            /*Return if the "tree" is undefined*/
            if (WM.isUndefined(tree)) {
                return;
            }
            /*If parentNodeUId is not specified, insert the node into the tree and return*/
            if (WM.isUndefined(parentNodeUId) && WM.isUndefined(parentNodeId)) {
                parentNode = tree[0];
            } else {
                if (parentNodeId) {
                    parentNodeUId = getNodeFromJson(tree, parentNodeId).uid;
                }
                parentIndex = parentNodeUId.split("-");
                parentStartIndex = parentIndex.shift();
                parentNode = tree[parentStartIndex];
                parentIndex.forEach(function (index) {
                    parentNode = parentNode.children[index];
                });
            }

            /*Case: The tree has no children*/
            if (!(parentNode.children) || !(WM.isArray(parentNode.children))) {
                /*Initialize the tree children*/
                parentNode.children = [];
            } else {
                /*Check if a node with the specified id already exists in the tree children and return if found*/
                existingNode = getNode(parentNode.children, nodeId);
                if (existingNode) {
                    return existingNode;
                }
            }
            node.uid = parentNode.uid + "-" + (parentNode.children.length);
            /*Insert the node as a child to the tree and return*/
            parentNode.children.push(node);

            tree[0].nodeMap[node.uid] = nodeId;
            return node;
        }

        function removeJsonNodeChildren(tree, nodeId, parentNodeId) {
            var node = getNodeFromJson(tree, nodeId, parentNodeId);
            /*Check for sanity.*/
            if (node) {
                node.children = undefined;
            }
        }

        function getValidJSON(content) {
            if (!content) {
                return false;
            }
            try {
                /*obtaining json from editor content string*/
                return JSON.parse(content);
            } catch (e) {
                /*terminating execution if new variable object is not valid json.*/
                return false;
            }
        }

        /* prettify the js content */
        function prettifyJS(content) {
            if (window.js_beautify) {
                return window.js_beautify(content);
            }
            loadScriptsInSync(["_static_/components/js-beautify/js/lib/beautify.js"]);
            return window.js_beautify(content);
        }

        /* prettify the html content */
        function prettifyHTML(content) {
            if (window.html_beautify) {
                return window.html_beautify(content);
            }
            loadScriptsInSync(["_static_/components/js-beautify/js/lib/beautify-html.js"]);
            return window.html_beautify(content);
        }

        /* prettify the css content */
        function prettifyCSS(content) {
            if (window.css_beautify) {
                return window.css_beautify(content);
            }
            loadScriptsInSync(["_static_/components/js-beautify/js/lib/beautify-css.js"]);
            return window.css_beautify(content);
        }

        function getActionFromKey(event) {

            var ctrlOrMetaKey = isAppleProduct ? event.metaKey : event.ctrlKey,
                altKey = event.altKey;
            if (isAppleProduct) {
                if (event.which === 8) {
                    return 'DELETE';
                }
            } else {
                if (event.which === 46) {
                    return 'DELETE';
                }
            }

            if (ctrlOrMetaKey) {
                switch (event.which) {
                case 82:
                    return altKey ? "RUN" : "UNKNOWN";
                case 68:
                    return altKey ? "DEPLOY" : "UNKNOWN";
                case 83:
                    return 'SAVE';
                case 88:
                    return 'CUT';
                case 67:
                    return 'COPY';
                case 86:
                    return 'PASTE';
                case 90:
                    return event.shiftKey ? 'REDO' : 'UNDO';
                }
            } else {
                switch (event.which) {
                case 13:
                    return 'ENTER';
                case 27:
                    return 'ESC';
                case 38:
                    return 'UP-ARROW';
                case 40:
                    return 'DOWN-ARROW';
                }
            }

            return 'UNKNOWN';
        }

        function preventCachingOf(url) {
            if (!url) {
                return;
            }

            var _url = url + (url.indexOf("?") !== -1 ? "&" : "?");
            _url += "preventCache=" + Date.now();

            return _url;
        }

        function getAllKeysOf(obj, prefix) {
            var keys = [];
            prefix = prefix ? prefix + "." : "";

            if (WM.isObject(obj) && !WM.isArray(obj)) {
                Object.keys(obj).forEach(function (key) {
                    keys.push(prefix + key);
                    keys = keys.concat(getAllKeysOf(obj[key], prefix + key));
                });
            }
            return keys;
        }

        /* returns the requested service if available */
        function getService(serviceName) {
            if (!serviceName) {
                return;
            }
            /* get a reference to the element where ng-app is defined */
            var appEl = WM.element("[data-ng-app]"), injector;
            if (appEl) {
                try {
                    injector = appEl.injector(); // get the angular injector
                    if (injector) {
                        return injector.get(serviceName); // return the service
                    }
                } catch (e) {
                    return undefined;
                }
            }
        }

        /*removes protocol information from url*/
        function removeProtocol(url) {
            var newUrl;
            if (url.indexOf('http') !== -1) {
                /*Removing the protocol from the url*/
                if (url.indexOf('https:') !== -1) {
                    newUrl = url.substr(6);
                } else {
                    newUrl = url.substr(5);
                }
            } else {
                newUrl = url;
            }
            return newUrl;
        }

        function getCookieByName(name) {
            var cookiesArray = document.cookie.split("; "),
                cookies = {};
            cookiesArray.forEach(function (cookie) {
                var cookieArray = cookie.split("=");
                cookies[cookieArray[0]] = cookieArray[1];
            });
            return cookies[name];
        }

        /*Function to check whether the specified object is a pageable object or not.*/
        function isPageable(obj) {
            var pageable = {
                "content": [],
                "first": true,
                "firstPage": true,
                "last": true,
                "lastPage": true,
                "number": 0,
                "numberOfElements": 10,
                "size": 20,
                "sort": null,
                "totalElements": 10,
                "totalPages": 1
            };
            return (WM.equals(Object.keys(pageable), Object.keys(obj).sort()));
        }

        /* returns true if HTML5 File API is available else false*/
        function isFileUploadSupported() {
            return (window.File && window.FileReader && window.FileList && window.Blob);
        }

        function processMarkup(markupStr) {
            if (!markupStr) {
                return '';
            }

            var content = markupStr.replace(/>\s+</g, '><'),
                root = WM.element('<div></div>');

            /* wm-livelist and wm-login elements will have ngController directive this will result in
             * error:multidir Multiple Directive Resource Contention
             * to resolve this issue,
             * RunMode: remove the ngController directive from the element and add a wrapper with the controller name
             * StudioMode: remove the ngController directive
             */
            if (APPCONSTANTS.isRunMode) {
                root.html(content).find('wm-livelist, wm-login')
                    .each(function () {
                        var widget = WM.element(this),
                            wrapper,
                            ctrlName = widget.attr('data-ng-controller');

                        if (ctrlName) {
                            wrapper = WM.element('<div></div>').attr('data-ng-controller', ctrlName);
                            widget.removeAttr('data-ng-controller').wrap(wrapper);
                        }
                    });
                content = root[0].innerHTML;
            }

            return content;
        }
        function getFieldTypeWidgetTypesMap() {
            return fieldTypeWidgetTypeMap;
        }

        /*Function that checks if the dataset is valid or not*/
        function isValidDataSet(dataset) {
            return ((WM.isArray(dataset) && dataset.length > 0) || (WM.isObject(dataset) && Object.keys(dataset).length > 0));
        }

        /* to generate all individual contents from the combined version(min.html) of the page */
        function parseCombinedPageContent(pageContent, pageName) {
            /*creating a parent for the content & converting to dom-like element, to process the content*/
            var pageDom = WM.element("<div>" + pageContent + "</div>"),
                htmlEle = pageDom.find('script[id="' + pageName + '.html' + '"]'),
                variableContext = "_" + pageName + "Page_Variables_";
            /* remove the previously loaded styles in studio-mode*/
            if (APPCONSTANTS.isStudioMode) {
                WM.element('script[id="' + pageName + '.css' + '"]').remove();
            }
            try {
                /*load the styles & scripts*/
                WM.element('head').append(pageDom.find('style, script'));
            } catch (e) {
                console.log(e.message);
            }
            return {
                html: htmlEle.html() || '',
                variables: window[variableContext] || {}
            };
        }

        /*
         * extracts and returns the last bit from full typeRef of a field
         * e.g. returns 'String' for typeRef = 'java.lang.String'
         * @params: {typeRef} type reference
         */
        function extractType(typeRef) {
            if (!typeRef) {
                return 'string';
            }
            return typeRef.substring(typeRef.lastIndexOf(".") + 1);
        }

        /* returns true if the provided data type matches number type */
        function isNumberType(type) {
            return (NUMBER_TYPES.indexOf(extractType(type).toLowerCase()) !== -1);
        }

        function isDeleteResourceAllowed(context, path) {
            return (!SYSTEM_FOLDER_PATHS[context] || SYSTEM_FOLDER_PATHS[context].indexOf(path) === -1);
        }

        /*Function to generate a random number*/
        function random() {
            return Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1);
        }

        /*Function to generate a guid based on random numbers.*/
        function generateGUId() {
            return random() + '-' + random() + '-' + random();
        }

        return {
            camelCase: WM.element.camelCase,
            initCaps: initCaps,
            periodSeparate: periodSeparate,
            spaceSeparate: spaceSeparate,
            prettifyLabel: prettifyLabel,
            getImageUrl: getImageUrl,
            getResourceUrl: getResourceURL,
            formatVariableIconClass: formatVariableIconClass,
            getBackGroundImageUrl: getBackGroundImageUrl,
            getParentOverlayElZIndex: getParentOverlayElZIndex,
            hyphenate: hyphenate,
            deHyphenate: deHyphenate,
            isAndroid: isAndroid,
            isAndroidPhone: isAndroidPhone,
            isIphone: isIphone,
            isIpod: isIpod,
            isMobile: isMobile,
            isScriptLoaded: isScriptLoaded,
            isValidJavaPackageName: isValidJavaPackageName,
            stringStartsWith: stringStartsWith,
            stringEndsWith: stringEndsWith,
            isStyleSheetLoaded: isStyleSheetLoaded,
            isValidWebURL: isValidWebURL,
            loadScripts: loadScripts,
            loadStyleSheets: loadStyleSheets,
            loadStyleSheet: loadStyleSheet,
            prepareFieldDefs: prepareFieldDefs,
            prettifyCSS: prettifyCSS,
            prettifyHTML: prettifyHTML,
            prettifyJS: prettifyJS,
            swapArrayElements: swapArrayElements,
            swapProperties: swapProperties,
            triggerFn: triggerFn,
            isEmptyObject: isEmptyObject,
            isValidEmail: isValidEmail,
            resetObjectWithEmptyValues: resetObjectWithEmptyValues,
            isImageFile: isImageFile,
            isZipFile: isZipFile,
            isExeFile: isExeFile,
            isTextLikeFile: isTextLikeFile,
            isAudioFile: isAudioFile,
            isVideoFile: isVideoFile,
            isPageResource: isPageResource,
            findValueOf: findValueOf,
            replace: replace,
            removeStyleSheetLoaded: removeStyleSheetLoaded,
            fileUploadFallback: fileUploadFallback,
            getCurrentPage: getCurrentPage,
            isDebugMode: isDebugMode,
            getIndexPage: getIndexPage,
            redirectToIndexPage: redirectToIndexPage,
            redirectToLoginPage: redirectToLoginPage,
            browserStorage: browserStorage,
            fetchPropertiesMapColumns: fetchPropertiesMapColumns,
            getLoadedPrefabNames: getLoadedPrefabNames,
            initializeAction: initializeAction,
            addNodeToJson: addNodeToJson,
            getNodeFromJson: getNodeFromJson,
            removeJsonNodeChildren: removeJsonNodeChildren,
            isIE: isIE,
            isIE11: isIE11,
            getValidJSON: getValidJSON,
            getActionFromKey: getActionFromKey,
            preventCachingOf: preventCachingOf,
            getAllKeysOf: getAllKeysOf,
            fetchContent: fetchContent,
            getService: getService,
            removeProtocol: removeProtocol,
            getCookieByName: getCookieByName,
            isPageable: isPageable,
            isNumberType: isNumberType,
            isFileUploadSupported: isFileUploadSupported,
            processMarkup: processMarkup,
            getFieldTypeWidgetTypesMap: getFieldTypeWidgetTypesMap,
            isValidDataSet: isValidDataSet,
            parseCombinedPageContent: parseCombinedPageContent,
            extractType: extractType,
            isDeleteResourceAllowed: isDeleteResourceAllowed,
            generateGUId: generateGUId
        };
    }]);
