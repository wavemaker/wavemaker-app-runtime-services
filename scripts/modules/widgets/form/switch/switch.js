/*global WM */
/*Directive for switch */

WM.module('wm.widgets.form')
    .run([
        '$templateCache',
        '$rootScope',

        function ($templateCache, $rootScope) {
            'use strict';
            $templateCache.put('template/widget/form/switch.html',
                '<div data-ng-show="show" class="app-switch" init-widget has-model ' + $rootScope.getWidgetStyles() + '>' +
                    '<div class="btn-group">' +
                        '<button class="btn btn-default" data-ng-disabled="disabled" ' +
                            ' data-ng-style="{\'width\': btnwidth + \'%\'}"' +
                            ' data-ng-repeat="opt in options track by $index" data-ng-class="{active: selected.index === $index}"' +
                            ' data-ng-click="selectOpt($event, $index)">{{opt[displayfield || "label"]}}</button>' +
                        '<span class="btn btn-primary" data-ng-style="{\'width\': btnwidth + \'%\'}"></span>' +
                    '</div>' +
                '<input type="hidden" class="ng-hide model-holder" data-ng-disabled="disabled" value="{{_model_}}">' +
                '</div>'
                );
        }
    ])
    .directive('wmSwitch', [
        'PropertiesFactory',
        'WidgetUtilService',
        'CONSTANTS',

        function (PropertiesFactory, WidgetUtilService, CONSTANTS) {
            'use strict';

            var widgetProps = PropertiesFactory.getPropertiesOf('wm.switch', ['wm.base', 'wm.base.editors', 'wm.base.editors.abstracteditors']),
                notifyFor = {
                    'dataset': true
                },
                COMMA_SEP_STRING = 1,
                ARRAY_STRINGS = 2,
                ARRAY_OBJECTS = 3,
                NONE = 0;

            function updatePropertyPanelOptions(dataset, propertiesMap, scope) {
                var variableKeys = [];
                /* on binding of data*/
                if (dataset && WM.isObject(dataset)) {
                    dataset = dataset[0] || dataset;
                    variableKeys = WidgetUtilService.extractDataSetFields(dataset, propertiesMap) || [];
                }

                /*removing null values from the variableKeys*/
                WM.forEach(variableKeys, function (variableKey, index) {
                    if (dataset[variableKey] === null || WM.isObject(dataset[variableKey])) {
                        variableKeys.splice(index, 1);
                    }
                });

                /* re-initialize the property values */
                if (scope.newcolumns) {
                    scope.newcolumns = false;
                    scope.datafield = '';
                    scope.displayfield = '';
                    scope.$root.$emit("set-markup-attr", scope.widgetid, {'datafield': scope.datafield, 'displayfield': scope.displayfield});
                }

                scope.widgetProps.datafield.options = ['', 'All Fields'].concat(variableKeys);
                scope.widgetProps.displayfield.options = [''].concat(variableKeys);
            }

            function trim(str) {
                if (WM.isString(str)) {
                    return str.trim();
                }
                return str;
            }

            function toOptionsObjFromString(str) {
                return {
                    'value': str,
                    'label': str
                };
            }

            function setSelectedValue(scope) {
                var options = scope.options;
                if (scope._model_ !== undefined && scope._model_ !== null) {
                    options.some(function (opt, index) {

                        if (scope._model_ === opt
                                || scope._model_ === opt[scope.datafield]
                                || scope._model_ === opt.value) {

                            scope.selected.index = index;

                            return true;
                        }
                    });
                }
            }

            function updateHighlighter(scope, element) {
                var handler = element.children().first().find('span.btn-primary'),
                    left,
                    index = scope.selected.index,
                    isToggle = scope.options.length === 2;
                if (index === undefined || index === null) {
                    index = -1;
                }

                if (isToggle && index !== -1) {
                    index = index === 0 ? 1 : 0;
                }

                left = index * scope.btnwidth;

                if (!isToggle) {
                    handler.addClass('switch-handle');
                } else {
                    handler.removeClass('switch-handle');
                }

                handler.animate({
                    left: left + '%'
                }, 300);
            }

            function updateSwitchOptions(scope, element, dataset) {
                var options = [];

                scope.selected.index = -1;
                scope.datasetType = NONE;
                dataset = dataset ? dataset.data || dataset : [];

                if (WM.isString(dataset)) { // comma separated strings
                    options = dataset.split(',').map(trim).map(toOptionsObjFromString);
                    scope.datasetType = COMMA_SEP_STRING;
                } else if (WM.isObject(dataset)) { // array or object
                    if (WM.isArray(dataset)) { // array
                        if (WM.isString(dataset[0])) { // array of strings
                            options = dataset.map(trim).map(toOptionsObjFromString);
                            scope.datasetType = ARRAY_STRINGS;
                        } else if (WM.isObject(dataset[0]) && !WM.isArray(dataset[0])) { // array of objects
                            options = dataset;
                            scope.datasetType = ARRAY_OBJECTS;
                        }
                    }
                }

                if (options.length) {
                    scope.btnwidth = (100 / options.length);
                }

                scope.options = options;

                setSelectedValue(scope);
                updateHighlighter(scope, element);
            }

            /* Define the property change handler. This function will be triggered when there is a change in the widget property */
            function propertyChangeHandler(scope, element, key, newVal) {
                switch (key) {
                case 'dataset':
                    if (scope.widgetid && WM.isDefined(newVal) && newVal !== null) {
                        updatePropertyPanelOptions(newVal.data || newVal, newVal.propertiesMap, scope);
                    }
                    updateSwitchOptions(scope, element, newVal);
                    break;
                }
            }

            return {
                'restrict': 'E',
                'replace': true,
                'scope': {
                    'scopedataset': '=?'
                },
                'template': function (tElement, tAttrs) {
                    var template = WM.element(WidgetUtilService.getPreparedTemplate('template/widget/form/switch.html', tElement, tAttrs));
                    return template[0].outerHTML;
                },
                'compile': function () {
                    return {
                        'pre': function (scope) {
                            scope.widgetProps = widgetProps;
                        },
                        'post': function (scope, element, attrs) {

                            scope.options = [];

                            /* fields defined in scope: {} MUST be watched explicitly */

                            if (!scope.widgetid && attrs.hasOwnProperty('scopedataset')) {
                                scope.$watch('scopedataset', function (newVal) {
                                    /*generating the radioset based on the values provided*/
                                    updateSwitchOptions(scope, element, newVal);
                                });
                            }

                            scope.selectOptAtIndex = function ($index) {
                                var opt = scope.options[$index];
                                if (scope.datasetType === ARRAY_OBJECTS) {
                                    if (scope.datafield) {
                                        if (scope.datafield === 'All Fields') {
                                            scope._model_ = opt;
                                        } else {
                                            scope._model_ = opt[scope.datafield];
                                        }
                                    }
                                } else {
                                    scope._model_ = opt.value;
                                }
                            };

                            scope.selectOpt = function ($event, $index) {
                                if (scope.disabled) {
                                    return;
                                }

                                if (scope.selected.index === $index) {
                                    if (scope.options.length === 2) {
                                        $index = $index === 1 ? 0 : 1;
                                    } else {
                                        return;
                                    }
                                }
                                scope.selected.index = $index;

                                scope.selectOptAtIndex($index);
                                updateHighlighter(scope, element);

                                scope._onChange($event);
                            };

                            scope.$watch('_model_', function () {
                                setSelectedValue(scope);
                                updateHighlighter(scope, element);
                            });

                            scope.selected = {};

                            WidgetUtilService.registerPropertyChangeListener(propertyChangeHandler.bind(undefined, scope, element), scope, notifyFor);
                            WidgetUtilService.postWidgetCreate(scope, element, attrs);
                        }
                    };
                }
            };
        }]);
/**
 * @ngdoc directive
 * @name wm.widgets.form.directive:wmSwitch
 * @restrict E
 *
 * @description
 * The `wmSwitch` directive defines the switch widget.
 *
 * @scope
 *
 * @requires PropertiesFactory
 * @requires WidgetUtilService
 * @requires CONSTANTS
 *
 * @param {string=} name
 *                  Name of the switch widget.
 * @param {string=} hint
 *                  Title/hint for the switch. <br>
 *                  This property is bindable.
 * @param {string=} caption
 *                  This property defines two states of switch.Comma separated values example: On, Off <br>
 *                  Default value: `On, Off`. <br>
 * @param {string=} width
 *                  Width of the switch.
 * @param {string=} height
 *                  Height of the switch.
 * @param {boolean=} show
 *                  Show is a bindable property. <br>
 *                  This property will be used to show/hide the switch widget on the web page. <br>
 *                  Default value: `true`. <br>
 * @param {boolean=} checked
 *                   This property will be used to set the initial state of the switch widget. <br>
 *                   Default value: `false`. <br>
 * @param {string=} checkedvalue
 *                  This property defines the value of the switch widget when the element is in the checked state. Mandatory for displaying widget value.<br>
 *                  Default value: `false`. <br>
 * @param {string=} scopedatavalue
 *                  This property accepts the value for the switch widget from a variable defined in the script workspace. <br>
 * @param {string=} on-click
 *                  Callback function for `click` event.
 * @param {string=} on-change
 *                  Callback function for `change` event.
 * @param {string=} on-mouseenter.
 *                  Callback function for `mouseenter` event.
 * @param {string=} on-mouseleave
 *                  Callback function for `mouseleave` event.
 * @param {string=} on-focus
 *                  Callback function for `focus` event.
 * @param {string=} on-blur
 *                  Callback function for `blur` event.
 * @example
 *   <example module="wmCore">
 *       <file name="index.html">
 *           <div data-ng-controller="Ctrl" class="wm-app">
 *               <div>single click count: {{clickCount}}</div>
 *               <div>change count: {{changeCount}}</div>
 *               <div>mouse enter count: {{mouseenterCount}}</div>
 *               <div>mouse leave count: {{mouseleaveCount}}</div>
 *               <div>focus count: {{focusCount}}</div>
 *               <div>blur count: {{blurCount}}</div>
 *
 *               <wm-composite>
 *                   <wm-label caption="{{switchlabel}}"></wm-label>
 *                   <wm-switch
 *                       hint="hint/title for switch"
 *                       scopedataset="switchoptions"
 *                       on-click="f('click');"
 *                       on-change="f('change');"
 *                       on-focus="f('focus');"
 *                       on-blur="f('blur');"
 *                       on-mouseenter="f('mouseenter');"
 *                       on-mouseleave="f('mouseleave')"
 *                       width="{{width}}"
 *                       height="{{height}}">
 *                   </wm-switch>
 *               </wm-composite>
 *
 *               <div>Switch state: {{favitem1}}</div>
 *
 *               <wm-composite>
 *                   <wm-label caption="Options"></wm-label>
 *                   <wm-text scopedatavalue="switchoptions" placeholder="enter comma separated values"></wm-text>
 *               </wm-composite>
 *               <wm-composite>
 *                   <wm-label caption="width:"></wm-label>
 *                   <wm-text scopedatavalue="width"></wm-text>
 *               </wm-composite>
 *               <wm-composite>
 *                   <wm-label caption="height:"></wm-label>
 *                   <wm-text scopedatavalue="height"></wm-text>
 *               </wm-composite>
 *           </div>
 *       </file>
 *       <file name="script.js">
 *          function Ctrl($scope) {
 *              $scope.clickCount =
 *              $scope.changeCount =
 *              $scope.mouseenterCount =
 *              $scope.mouseleaveCount =
 *              $scope.focusCount =
 *              $scope.blurCount = 0;
 *              $scope.favcolors = [];
 *
 *              $scope.width = "120px";
 *              $scope.height= "30px";
 *
 *              $scope.switchoptions = "on,off";
 *
 *              $scope.f = function (eventtype) {
 *                  $scope[eventtype + 'Count']++;
 *              }
 *           }
 *       </file>
 *   </example>
 */