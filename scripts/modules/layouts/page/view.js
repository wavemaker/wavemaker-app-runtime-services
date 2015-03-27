/*global WM*/

WM.module('wm.layouts.page')
    .run(['$templateCache', '$rootScope', function ($templateCache, $rootScope) {
        'use strict';
        $templateCache.put('template/layout/page/view.html',
                '<div init-widget class="app-view" data-ng-show="show" ' + $rootScope.getWidgetStyles('container') + ' wmtransclude wm-navigable-element="true"> </div>'
            );
    }])
    .directive('wmView', ['PropertiesFactory', 'WidgetUtilService', 'CONSTANTS', 'ViewService', 'Utils', function (PropertiesFactory, WidgetUtilService, CONSTANTS, ViewService, Utils) {
        'use strict';
        var widgetProps = PropertiesFactory.getPropertiesOf('wm.layouts.view', ['wm.layouts', 'wm.base.events.touch']);

        return {
            'restrict': 'E',
            'replace': true,
            'scope': {},
            'transclude': true,
            'template': WidgetUtilService.getPreparedTemplate.bind(undefined, 'template/layout/page/view.html'),
            'compile': function () {
                return {
                    'pre': function (scope) {
                        /*Applying widget properties to directive scope*/
                        scope.widgetProps = widgetProps;
                    },
                    'post': function (scope, element, attrs) {
                        ViewService.registerView(scope);
                        scope.setActive = function () {
                            ViewService.showView(scope.name);
                        };
                        var isDialogView = element.hasClass('dialog-view');
                        if (isDialogView) {
                            if (CONSTANTS.isStudioMode) {
                                /* dialog view is meant to have the dialog only, widgets should not be dropped on the same,
                                 * so making the dialog-view non-droppable
                                 * */

                                element.attr('wm-droppable', false);
                            }

                            if (CONSTANTS.isRunMode) {
                                /* hiding the dialog-view in run mode, just opening the dialog*/
                                scope.show = false;
                            }
                        }
                        scope.initialize = function () {
                            if (!scope.initialized) {
                                scope.initialized = true;
                                element.find('.ng-isolate-scope')
                                    .each(function () {
                                        Utils.triggerFn(WM.element(this).isolateScope().redraw);
                                    });
                            }
                        };

                        /* In studio mode, on canvas resize, unset initialized flags for hidden views, so the redrawable contents can be redrawn */
                        if (CONSTANTS.isStudioMode) {
                            scope.$on('$destroy', scope.$root.$on('canvas-resize', function () {
                                scope.initialized = false;
                            }));
                        }

                        WidgetUtilService.postWidgetCreate(scope, element, attrs);
                        element.on('$destroy', function () {
                            ViewService.unregisterView(scope);
                        });
                    }
                };
            }
        };
    }]);

/**
 * @ngdoc directive
 * @name wm.layouts.page.directive:wmView
 * @restrict E
 *
 * @description
 * The 'wmView' directive defines a view in the layout.
 * View is a container which can be added onto some specific containers (example - page, column).
 *
 * @scope
 *
 * @requires PropertiesFactory
 * @requires WidgetUtilService
 * @requires CONSTANTS
 * @requires ViewService
 *
 * @param {string=} horizontalalign
 *                  Align the content in the right panel to left/right/center.<br>
 *                  Default value for horizontalalign is `left`.
 * @param {string=} on-swipeup
 *                  Callback function for `swipeup` event.
 * @param {string=} on-swipedown
 *                  Callback function for `swipedown` event.
 * @param {string=} on-swiperight
 *                  Callback function for `swiperight` event.
 * @param {string=} on-swipeleft
 *                  Callback function for `swipeleft` event.
 * @param {string=} on-pinchin
 *                  Callback function for `pinchin` event.
 * @param {string=} on-pinchdown
 *                  Callback function for `pinchdown` event.
 *
 * @example
 <example module="wmCore">
     <file name="index.html">
         <wm-view >
         </wm-view>
     </file>
 </example>
 */

