/*global WM, wmCoreModule, wmDialog*/
/*Directive for login dialog */

WM.module('wm.widgets.dialog')
    .run(["$templateCache", function ($templateCache) {
        "use strict";
        $templateCache.put("template/widget/dialog/logindialog.html",
            '<div class="app-dialog app-login-dialog" init-widget data-ng-show="show" ' +
                ' data-ng-style="{width: dialogWidth, height: dialogHeight}"' +
                ' wmtransclude>' +
                ' </div>'
            );
        $templateCache.put("template/widget/dialog/logindialogcontainer.html",
            '<div wmtransclude></div>'
            );
    }]).directive('wmLogindialog', ['PropertiesFactory', 'WidgetUtilService', "$templateCache", '$compile', 'CONSTANTS', function (PropertiesFactory, WidgetUtilService, $templateCache, $compile, CONSTANTS) {
        'use strict';
        var transcludedContent = "",
            id,
            widgetProps = PropertiesFactory.getPropertiesOf("wm.logindialog", ["wm.basicdialog", "wm.base", "wm.base.events.successerror"]),
            notifyFor = {
                'width': true,
                'height': true,
                'iconclass': true,
                'iconwidth': true,
                'iconheight': true,
                'iconmargin': true,
                'title': true
            };

        /* Define the property change handler. This function will be triggered when there is a change in the widget property */
        function propertyChangeHandler(scope, key, newVal) {
            switch (key) {
            case "iconclass":
            case "iconwidth":
            case "iconheight":
            case "iconmargin":
                scope.header[key] = newVal;
                break;
            case "title":
                scope.header.caption = newVal;
                break;
            case "width":
                if (CONSTANTS.isStudioMode) {
                    scope.dialogWidth = newVal;
                }
                break;
            case "height":
                scope.dialogHeight = newVal;
                break;
            }
        }

        return {
            "restrict": "E",
            "transclude": (CONSTANTS.isStudioMode),
            "scope": {
                "dialogid": '@'
            },
            "template": function (template, attrs) {
                transcludedContent = template.html();
                /*to have script tag with name as id in run mode and to have div in studio to be able to style the dialog*/
                if (CONSTANTS.isRunMode) {
                    /* replacing wm-logindialog with wm-dialog-container in run mode to have a container for header, content and footer.
                     wm-dialog-container has a template similar to wm-dialog, replacing since wm-dialog returns script tag*/
                    var dialogEle = WM.element(template[0].outerHTML),
                        onsuccess = dialogEle.attr('on-success') || '',
                        onerror = dialogEle.attr('on-error') || '',
                        dialog = template[0].outerHTML.replace("<wm-logindialog ", "<wm-dialog-container class='app-login-dialog' ");
                    dialog = dialog.replace("</wm-logindialog>", "</wm-dialog-container>");
                    dialog = '<wm-logindialog-container on-success="' + onsuccess + '" on-error="' + onerror + '">' + dialog + '</wm-logindialog-container>';
                    transcludedContent = dialog;
                    id = attrs.name;
                    return '<script type="text/ng-template" id="' + id + '">' + transcludedContent + "</script>";
                }
                return $templateCache.get("template/widget/dialog/logindialog.html");
            },
            "replace": true,
            "compile": function () {
                return {
                    "pre": function (scope) {
                        scope.widgetProps = widgetProps;
                    },
                    "post": function (scope, element, attrs) {

                        if (CONSTANTS.isStudioMode) {
                            element.append($compile(transcludedContent)(scope));
                            element.addClass('modal-content');
                        }
                        scope = scope || element.isolateScope();
                        scope.header = element.find('[data-identifier=dialog-header]').isolateScope() || {};
                        scope.content = element.find('[data-identifier=dialog-content]').isolateScope() || {};

                        /* register the property change handler */
                        if (scope.propertyManager) {
                            WidgetUtilService.registerPropertyChangeListener(propertyChangeHandler.bind(undefined, scope), scope, notifyFor);
                        }

                        WidgetUtilService.postWidgetCreate(scope, element, attrs);
                    }
                };
            }
        };
    }]).directive('wmLogindialogContainer', ["$templateCache", "PropertiesFactory", "WidgetUtilService", "SecurityService", "BaseService", "Utils", "CONSTANTS", function ($templateCache, PropertiesFactory, WidgetUtilService, SecurityService, BaseService, Utils, CONSTANTS) {
        'use strict';
        var widgetProps = PropertiesFactory.getPropertiesOf("wm.logindialog", ["wm.base"]);
        return {
            "restrict": "E",
            "transclude": true,
            "scope": {
                "dialogid": '@',
                "onSuccess": '&',
                "onError": '&'
            },
            "template": $templateCache.get("template/widget/dialog/logindialogcontainer.html"),
            "replace": true,
            "compile": function () {
                return {
                    "pre": function (scope) {
                        scope.widgetProps = widgetProps;
                    },
                    "post": function (scope, element, attrs) {
                        if (CONSTANTS.isRunMode) {
                            /*function to be called in case of login*/
                            scope.doLogin = function (event) {
                                scope.loginMessage = scope.$parent.loginMessage = null;
                                SecurityService.appLogin({
                                    username: element.find('[name="usernametext"]').val(),
                                    password: element.find('[name="passwordtext"]').val()
                                }, function () {
                                    scope.$root.isUserAuthenticated = true;
                                    element.trigger("success");
                                    scope.onSuccess({$event: event, $scope: scope});
                                    BaseService.executeErrorCallStack();
                                }, function (error) {
                                    scope.loginMessage = scope.$parent.loginMessage = {
                                        type: 'error',
                                        caption: scope.errormessage || error
                                    };
                                    element.trigger("error");
                                    scope.onError({$event: event, $scope: scope});
                                });
                            };
                            var loginbutton = element.find('.app-button[name="loginbutton"]');
                            /*to remove the on-click event handler*/
                            loginbutton.unbind('click');
                            /*bind sign-in functionality to the sign-in button*/
                            loginbutton.click(scope.doLogin.bind(null));
                            /*bind sign-in functionality to the sign-in button*/
                            element.find('.app-textbox').keypress(function (evt) {
                                evt.stopPropagation();
                                /*Trigger the action to "doLogin" if the "enter" key has been pressed.*/
                                if (Utils.getActionFromKey(evt) === "ENTER") {
                                    scope.doLogin(evt);
                                }
                            });
                        }

                        WidgetUtilService.postWidgetCreate(scope, element, attrs);
                    }
                };
            }
        };
    }]);

/**
 * @ngdoc directive
 * @name wm.widgets.dialog.directive:wmLogindialog
 * @restrict E
 *
 * @description
 * The `wmLogindialog` directive defines login dialog widget. <br>
 * An login dialog is created in an independent view.
 *
 * @scope
 *
 * @requires PropertiesFactory
 * @requires WidgetUtilService
 * @requires $templateCache
 * @requires CONSTANTS
 *
 * @param {string=} name
 *                  Name of the dialog.
 * @param {string=} title
 *                  title of the dialog.
 * @param {string=} width
 *                  Width of the dialog.
 * @param {boolean=} show
 *                  show is a bindable property. <br>
 *                  This property will be used to show/hide the dialog on the web page. <br>
 *                  Default value: `true`.
 * @param {string=} iconname
 *                  Icon sets the icon for dialog header
 *
 * @example
 *   <example module="wmCore">
 *       <file name="index.html">
 *           <wm-view name="view1" class="dialog-view">
 *               <wm-logindialog modal="false" iconname="log-in" title="Login" name="logindialog1" on-error="logindialog1Error($event, $scope)" on-success="logindialog1Success($event, $scope)">
 *                  <wm-dialogheader name="dialogheader1"></wm-dialogheader>
 *                  <wm-dialogcontent name="dialogcontent1">
 *                      <wm-form name="form1">
 *                          <wm-message type="error" caption="{{errMsg}}" show="{{showErrMsg}}" class="app-logindialog-message" hide-close="true" name="message1"></wm-message>
 *                          <wm-composite name="composite1">
 *                              <wm-label caption="Username" class="col-md-4" name="label5"></wm-label>
 *                              <wm-text placeholder="Enter username" class="app-logindialog-username" name="text1"></wm-text>
 *                          </wm-composite>
 *                          <wm-composite widget="text" name="composite2">
 *                              <wm-label caption="Password" class="col-md-4" name="label6"></wm-label>
 *                              <wm-text type="password" placeholder="Enter password" class="app-logindialog-password" name="text2"></wm-text>
 *                          </wm-composite>
 *                      </wm-form>
 *                  </wm-dialogcontent>
 *                  <wm-dialogactions name="dialogactions1">
 *                      <wm-button class="btn-primary" caption="Sign in" name="button10"></wm-button>
 *                  </wm-dialogactions>
 *              </wm-logindialog>
 *           </wm-view>
 *           <wm-button on-click="logindialog1.show" caption="show dialog"></wm-button>
 *       </file>
 *       <file name="script.js">
 *          function Ctrl($scope) {
 *
 *          }
 *       </file>
 *   </example>
 */
