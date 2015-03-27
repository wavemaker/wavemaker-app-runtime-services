/*global WM, */
/*Directive for spinner*/

WM.module('wm.widgets.basic')
    .run(['$templateCache', '$rootScope', function ($templateCache, $rootScope) {
        'use strict';
        $templateCache.put('template/widget/spinner.html',
            '<div class="app-spinner {{size}}" data-ng-show="show" init-widget title="{{hint}}" ' + $rootScope.getWidgetStyles() + ' >' +
                '<div class="spinner-message">' +
                    '<span class="spinner-image" data-ng-style="{backgroundImage:picture, width: imagewidth, height: imageheight}"></span>' +
                    '<span class="spinner-text" data-ng-bind="caption"></span>' +
                '</div>' +
            '</div>'
            );
    }]).directive('wmSpinner', ['PropertiesFactory', '$rootScope', '$templateCache', 'WidgetUtilService', 'Utils', function (PropertiesFactory, $rootScope, $templateCache, WidgetUtilService, Utils) {
        'use strict';
        var widgetProps = PropertiesFactory.getPropertiesOf('wm.spinner', ['wm.base']),
            notifyFor = {
                'image': true,
                'backgroundimage': true
            };

        /* Define the property change handler. This function will be triggered when there is a change in the widget property */
        function propertyChangeHandler(scope, key, newVal) {
            switch (key) {
            case 'image':
                scope.picture = Utils.getBackGroundImageUrl(newVal);
                break;
            case 'backgroundimage':
                scope.backgroundimgsource = Utils.getBackGroundImageUrl(newVal);
                break;
            }
        }

        return {
            'restrict': 'E',
            'scope': {},
            'replace': true,
            'template': $templateCache.get('template/widget/spinner.html'),
            'compile': function () {
                return {
                    'pre': function (scope) {
                        scope.widgetProps = widgetProps;
                    },
                    'post': function (scope, element, attrs) {
                        /* register the property change handler */
                        WidgetUtilService.registerPropertyChangeListener(propertyChangeHandler.bind(undefined, scope), scope, notifyFor);

                        WidgetUtilService.postWidgetCreate(scope, element, attrs);

                        if (!scope.widgetid) {
                            scope.$on('$destroy', $rootScope.$on('toggle-variable-state', function (event, variableName, show) {
                                if (variableName === scope.servicevariabletotrack) {
                                    scope.show = show;
                                }
                            }));
                        }
                    }
                };
            }
        };
    }]);

/**
 * @ngdoc directive
 * @name wm.widgets.basic.directive:wmSpinner
 * @restrict E
 *
 * @description
 * The `wmSpinner` directive defines the loader widget. <br>
 *
 * @scope
 *
 * @requires PropertiesFactory
 * @requires $templateCache
 * @requires WidgetUtilService
 *
 * @param {string=}  name
 *                   Name of the spinner widget.
 * @param {string=}  image
 *                   Source path of the image in the widget.
 * @param {string=}  backgroundimage
 *                   Source path of the background-image for the container in the widget.
 * @param {boolean=} show
 *                  Show is a bindable property. <br>
 *                  This property will be used to show/hide the label widget on the web page. <br>
 *                  Default value: `true`.
 *
 * @example
 *   <example module="wmCore">
 *       <file name="index.html">
 *           <div data-ng-controller="Ctrl" class="wm-app">
 *               <wm-content contentwidth="12" name="example-content" backgroundcolor='transparent'>
 *                  <wm-column columnwidth="3" border="0" name="options-column">
 *                      <wm-composite>
 *                          <wm-label caption="Show:"></wm-label>
 *                          <wm-checkbox name="checkbox1" scopedatavalue="show" checked="checked"></wm-checkbox>
 *                      </wm-composite>
 *                      <wm-composite>
 *                          <wm-label caption="Image Path:"></wm-label>
 *                          <wm-text scopedatavalue="imagepath"></wm-text>
 *                      </wm-composite>
 *                  </wm-column>
 *                  <wm-column columnwidth="9" border="0" name="spinner-column">
 *                      <wm-spinner show='{{show}}' name="loader" image="{{imagepath}}"></wm-search>
 *                  </wm-column>
 *               </wm-content>
 *           </div>
 *       </file>
 *       <file name="script.js">
 *          function Ctrl($scope) {
 *              $scope.show = true;
 *              $scope.imagepath = '../app/build/studio/images/loader.gif';
 *          }
 *       </file>
 *   </example>
 */