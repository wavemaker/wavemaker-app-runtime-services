/*global WM, wmCoreModule, wmDialog, _*/
/*Directive for alert dialog */

WM.module('wm.widgets.dialog')
    .run(["$templateCache", function ($templateCache) {
        "use strict";
        $templateCache.put("template/widget/dialog/alertdialog.html",
            '<div class="app-dialog modal-dialog app-alert-dialog" ng-class="{type:type}" dialogclass init-widget data-ng-show="show" data-ng-style="{width: dialogWidth}"><div class="modal-content">' +
                '<wm-dialogheader iconclass={{iconclass}} iconwidth={{iconwidth}} iconheight={{iconheight}} iconmargin={{iconmargin}} caption={{title}}></wm-dialogheader>' +
                '<div class="app-dialog-body modal-body" data-ng-style="{height:bodyHeight}">' +
                    '<p class="app-dialog-message text-{{alerttype}}"> {{message}}</p>' +
                '</div>' +
                '<div class="app-dialog-footer modal-footer">' +
                    '<wm-button  class="btn-primary"  caption={{oktext}} on-click="okButtonHandler()"></wm-button>' +
                '</div>' +
            '</div></div>'
            );
    }]).directive('wmAlertdialog', ["$templateCache", "PropertiesFactory", "WidgetUtilService", "CONSTANTS", '$window', function ($templateCache, PropertiesFactory, WidgetUtilService, CONSTANTS, $window) {
        'use strict';
        var widgetProps = PropertiesFactory.getPropertiesOf("wm.alertdialog", ["wm.basicdialog", "wm.base", "wm.dialog.onOk"]),
            notifyFor = {
                'message': true,
                'oktext': true,
                'height': true,
                'width' : true,
                'type': true
            };

        /* Define the property change handler. This function will be triggered when there is a change in the widget property */
        function propertyChangeHandler(scope, element, attrs, key, newVal) {
            switch (key) {
            case "height":
                if (scope.height) {
                    //set the height for the Run Mode
                    if(newVal.indexOf('%') > 0 ){
                        scope.bodyHeight = ($window.innerHeight*(parseInt(newVal)/100) - 112);
                    } else {
                        scope.bodyHeight = parseInt(newVal - 112);
                    }
                }
                break;
            case "message":
                /*handling default values for notification alert dialog in studio*/
                if (attrs.notificationdialog && !CONSTANTS.isRunMode) {
                    scope.message = "Alert Notification Message";
                }
                break;
            case "oktext":
                /*handling default values for notification alert dialog in studio */
                if (attrs.notificationdialog && !CONSTANTS.isRunMode) {
                    scope.oktext = "OK";
                }
                break;
            case "width":
                if(scope.width && CONSTANTS.isRunMode){
                    //update the modal element in the UI for getting shadow and width set
                    element.closest('.modal-dialog').css('width', newVal);
                }else if(CONSTANTS.isStudioMode){
                    scope.dialogWidth = newVal;
                }
                break;
            }
        }

        return {
            "restrict": "E",
            "controller": "DialogController",
            "scope": {
                "dialogid": '@',
                "onOk": '&',
                "onClose": '&'
            },
            "replace": true,
            "template": function (template, attrs) {
                /*if the script tag has not been created already, set inscript to false*/
                if (template.attr('inscript') === undefined) {
                    template.attr('inscript', false);
                }
                /* in run mode, when script tag is not created, create script, else return normal template*/
                if (CONSTANTS.isRunMode && (template.attr('inscript') === "false")) {
                    /*once script tag is created, set inscript attribute to true*/
                    template.attr('inscript', true);
                    var transcludedContent = template[0].outerHTML,
                        id = attrs.name;
                    /*alert dialog is always modal, so setting backdrop to static*/
                    return '<script backdrop="static" type="text/ng-template" id="' + id + '">' + transcludedContent + "</script>";
                }
                return $templateCache.get("template/widget/dialog/alertdialog.html");
            },
            "compile": function () {
                return {
                    "pre": function (scope, element, attrs) {
                        scope.widgetProps = WM.copy(widgetProps);

                        /* for the notification-alert dialogs do not allow the user to edit the properties other than class */
                        if (attrs.widgetid && attrs.notificationdialog) { //widget is in canvas
                            _.forEach(Object.keys(scope.widgetProps), function (propName) {
                                if (propName !== "class") {
                                    scope.widgetProps[propName].disabled = true;
                                }
                            });
                        }
                    },
                    "post": function (scope, element, attrs, dialogCtrl) {
                        /* handles ok button click*/
                        if (!scope.okButtonHandler) {
                            scope.okButtonHandler = function () {
                                dialogCtrl._OkButtonHandler(attrs.onOk);
                            };
                        }

                        if (CONSTANTS.isStudioMode) {
                            element.addClass('modal-content');
                        }

                        /* register the property change handler */
                        if (scope.propertyManager) {
                            WidgetUtilService.registerPropertyChangeListener(propertyChangeHandler.bind(undefined, scope, element, attrs), scope, notifyFor);
                        }

                        WidgetUtilService.postWidgetCreate(scope, element, attrs);
                    }
                };
            }
        };
    }]);

/**
 * @ngdoc directive
 * @name wm.widgets.dialog.directive:wmAlertdialog
 * @restrict E
 *
 * @description
 * The `wmAlertdialog` directive defines alert dialog widget. <br>
 * An alert dialog is created in an independent view.
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
 * @param {string=} height
 *                  Height of the dialog.
 * @param {string=} width
 *                  Width of the dialog.
 * @param {string=} message
 *                  message is a bindable property. <br>
 *                  message to be shown in the dialog.
 * @param {string=} oktext
 *                  oktext is a bindable property. <br>
 *                  Text to be shown in dialog's Ok button.
 * @param {boolean=} show
 *                  show is a bindable property. <br>
 *                  This property will be used to show/hide the dialog on the web page. <br>
 *                  Default value: `true`.
 * @param {string=} alerttype
 *                  alerttype sets the type for the alert dialog.
 *                  Valid values are /information/error/success/warning
 * @param {string=} iconclass
 *                  Icon sets the icon for dialog header
 * @param {string=} on-close
 *                  Callback function which will be triggered when the dialog is closed.
 * @param {string=} on-ok
 *                  Callback function which will be triggered when the ok button for the dialog is clicked.
 *
 * @example
 *   <example module="wmCore">
 *       <file name="index.html">
 *           <wm-view name="view1" class="dialog-view">
 *               <wm-alertdialog name="alertdialog1" controller="Ctrl" iconclass="glyphicon glyphicon-warning-sign" alerttype="information" message="I am an alert box" oktext="OK Button" on-ok="onOkCallBack()" on-close="onCloseCallBack()">
 *               </wm-alertdialog>
 *           </wm-view>
 *           <wm-button on-click="alertdialog1.show" caption="show dialog"></wm-button>
 *       </file>
 *       <file name="script.js">
 *          function Ctrl($scope) {
 *              $scope.onCloseCallBack = function () {
 *                  console.log("inside close callback");
 *              }
 *              $scope.onOkCallBack = function () {
 *                  console.log("inside ok callback");
 *              }
 *          }
 *       </file>
 *   </example>
 */
