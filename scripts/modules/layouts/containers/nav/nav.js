/*global WM*/
/*Directive for Nav and NavItem*/

/*Directive for Nav*/

WM.module('wm.layouts.containers')
    .directive('wmNav', ['PropertiesFactory', 'WidgetUtilService', '$rootScope', '$compile', '$timeout', function (PropertiesFactory, WidgetUtilService, $rootScope, $compile, $timeout) {
        'use strict';
        var widgetProps = PropertiesFactory.getPropertiesOf('wm.layouts.nav', ['wm.layouts']),
            notifyFor = {
                'dataset': true,
                'scopedataset': true,
                'itemicon': true,
                'itemlabel': true,
                'itemlink': true,
                'itemchildren': true
            };

        function getNodes(scope, newVal) {
            var nodes = [];
            if (WM.isString(newVal)) {
                newVal = newVal.trim();
                if (newVal) {
                    nodes = newVal.split(',').map(function (item) {
                        return {
                            'label': item && item.trim()
                        };
                    });
                }
            } else if (WM.isArray(newVal)) {
                nodes = newVal;
            } else if (WM.isObject(newVal)) {
                nodes = [newVal];
            }

            if (scope.widgetid) { // when the widget is inside canvas
                scope.keys = WM.isObject(nodes[0]) ? Object.keys(nodes[0]) : [];
                /*Changing the properties like labels,children and icons*/
                scope.widgetProps.itemlabel.options = scope.widgetProps.itemchildren.options = scope.widgetProps.itemicon.options = scope.widgetProps.itemlink.options = scope.keys;
            }
            return nodes;
        }
        function constructNav(element, scope) {
            element.empty();
            if (scope.nodes && scope.nodes.length) {
                var iconField = scope.itemicon || 'icon',
                    labelField = scope.itemlabel || 'label',
                    itemField = scope.itemlink || 'link',
                    childrenField = scope.itemchildren || 'children';
                scope.nodes.forEach(function (node) {
                    var $anchor = WM.element('<a class="app-anchor"></a>'),
                        $list = WM.element('<li class="app-nav-item"></li>'),
                        $iconNode = WM.element('<i></i>'),
                        itemLabel = node[labelField],
                        itemClass = node[iconField],
                        itemLink = node[itemField],
                        itemChildren = node[childrenField],
                        $innerAnchor,
                        $innerList,
                        $innericonNode,
                        ulNode,
                        $caret = WM.element('<span class="caret"></span>');
                    if (itemChildren) {
                        $iconNode.addClass(itemClass);
                        $anchor.html(itemLabel).attr('dropdown-toggle', '').addClass('app-anchor dropdown-toggle').prepend($iconNode).append($caret);
                        $list.append($anchor).attr('dropdown', '').addClass('dropdown');
                        ulNode = WM.element('<ul class="dropdown-menu"></ul>');
                        itemChildren.forEach(function (child) {
                            $innerAnchor = WM.element('<a class="app-anchor"></a>');
                            $innerList = WM.element('<li class="app-nav-item"></li>');
                            $innericonNode = WM.element('<i></i>');
                            itemLabel = child[labelField];
                            itemClass = child[iconField];
                            itemLink = child[itemField];
                            $innericonNode.addClass(itemClass);
                            $innerAnchor.html(itemLabel).attr('href', itemLink).prepend($innericonNode);
                            $innerList.append($innerAnchor);
                            ulNode.append($innerList);
                        });
                        $list.append(ulNode);
                        element.append($list);
                    } else {
                        $iconNode.addClass(itemClass);
                        $anchor.html(itemLabel).attr('href', itemLink).prepend($iconNode);
                        $list.append($anchor);
                        element.append($list);
                    }
                    $compile($list)(scope);
                });
            }
        }


        /* Define the property change handler. This function will be triggered when there is a change in the widget property */
        function propertyChangeHandler(scope, element, key, newVal) {
            switch (key) {
            case 'dataset':
            case 'scopedataset':
                scope.nodes = getNodes(scope, newVal);
                constructNav(element, scope);
                if (scope.widgetid) {
                    $rootScope.$emit('nav-dataset-modified', {'widgetName': scope.name});
                }
                break;
            case 'itemicon':
            case 'itemlabel':
            case 'itemlink':
            case 'itemchildren':
                constructNav(element, scope);
                break;
            }

        }

        return {
            'restrict': 'E',
            'replace': true,
            'scope': {
                'scopedataset': '=?'
            },
            'transclude': true,
            'template': '<ul class="nav app-nav" ' +
                        ' data-ng-class="{\'nav-pills\': type == \'pills\',' +
                                         '\'nav-tabs\': type == \'tabs\',' +
                                         '\'navbar-nav\': type == \'navbar\',' +
                                         '\'nav-stacked\': layout == \'stacked\',' +
                                         '\'nav-justified\': layout == \'justified\'' +
                        '}"' +
                        'data-ng-show="show"' + $rootScope.getWidgetStyles('container') +
                        'data-element-type="wmNav" wmtransclude init-widget has-model ></ul>',
            'compile': function () {
                return {
                    'pre': function (scope) {
                        /*Applying widget properties to directive scope*/
                        scope.widgetProps = widgetProps;
                    },

                    'post': function (scope, element, attrs) {
                        /*Cleaning the widget markup such that the widget wrapper is not cluttered with unnecessary property or
                         * style declarations.*/
                        var onPropertyChange = propertyChangeHandler.bind(undefined, scope, element);

                        WidgetUtilService.registerPropertyChangeListener(onPropertyChange, scope, notifyFor);
                        if (!scope.widgetid && attrs.hasOwnProperty('scopedataset')) {
                            $timeout(function () {
                                scope.$watch('scopedataset', function (newVal) {
                                    onPropertyChange('scopedataset', newVal);
                                });
                            }, 0, true);
                        }
                        WidgetUtilService.postWidgetCreate(scope, element, attrs);
                    }
                };
            }
        };
    }])
    .directive('wmNavItem', ['PropertiesFactory', 'WidgetUtilService', '$rootScope', '$routeParams', 'CONSTANTS', function (PropertiesFactory, WidgetUtilService, $rootScope, $routeParams, CONSTANTS) {
        'use strict';
        var widgetProps = PropertiesFactory.getPropertiesOf('wm.layouts.navitem', ['wm.layouts']);

        return {
            'restrict': 'E',
            'replace': true,
            'scope': {},
            'transclude': true,
            'template': function (template) {
                if (template.find('[on-click="goToPage-' + (CONSTANTS.isRunMode ? $routeParams.name : $rootScope.activePageName)  + '"]').length) {
                    return '<li init-widget class="app-nav-item active"' + $rootScope.getWidgetStyles('container') + ' wmtransclude></li>';
                }
                return '<li init-widget class="app-nav-item"' + $rootScope.getWidgetStyles('container') + ' wmtransclude></li>';
            },
            'compile': function () {
                return {
                    'pre': function (scope) {
                        scope.widgetProps = widgetProps;
                    },

                    'post': function (scope, element, attrs) {
                        /*Cleaning the widget markup such that the widget wrapper is not cluttered with unnecessary property or
                         * style declarations.*/
                        WidgetUtilService.postWidgetCreate(scope, element, attrs);
                    }
                };
            }
        };
    }]);

/**
 * @ngdoc directive
 * @name wm.layouts.containers.directive:wmNav
 * @restrict E
 * @element ANY
 * @description
 * The 'wmNav' directive defines a dynamic navigation in the layout.
 * The 'wmNav' directive defines a nav in the layout to contain nav items.
 * @example
 *       <example module="wmCore">
 *          <file name="index.html">
 *              <div class="wm-app" data-ng-controller="Ctrl">
 *                  <wm-nav scopedataset="items"></wm-nav>
 *                 <wm-nav type="pills" layout="stacked">
 *                     <wm-nav-item>
 *                         <wm-anchor></wm-anchor>
 *                     </wm-nav-item>
 *                     <wm-nav-item>
 *                         <wm-anchor></wm-anchor>
 *                     </wm-nav-item>
 *                     <wm-nav-item>
 *                         <wm-anchor></wm-anchor>
 *                     </wm-nav-item>
 *                 </wm-nav>
 *              </div>
 *          </file>
 *          <file name="script.js">
 *             function Ctrl($scope) {
 *                  $scope.items = [
 *                       {
 *                           "label": "Home",
 *                           "icon": "glyphicon glyphicon-home",
 *                           "link": "#/home"
 *                       },
 *                       {
 *                           "label": "Dropdown",
 *                           "children": [
 *                               {
 *                                   "label": "Action",
 *                                   "icon": "glyphicon glyphicon-book"
 *                               },
 *                               {
 *                                   "label": "Help",
 *                                   "icon": "glyphicon glyphicon-question-sign"
 *                               }
 *                           ]
 *                       },
 *                       {
 *                           "label": "Others",
 *                           "icon": "glyphicon glyphicon-shopping-cart",
 *                           "link": "http://www.example.com"
 *                       },
 *                       {
 *                           "label": "Inventory",
 *                           "icon": "glyphicon glyphicon-tags"
 *                       }
 *                  ];
 *             };
 *          </file>
 *   </example>
 */
/**
 * @ngdoc directive
 * @name wm.layouts.containers.directive:wmNavItem
 * @restrict E
 * @element ANY
 * @description
 * The 'wmNavItem' directive defines a nav item in the layout.
 * wmNavItem is internally used by wmNav.
 * @example
 <example module="wmCore">
 <file name="index.html">
 <div  class="wm-app">
 <wm-nav>
 <wm-nav-item></wm-nav-item>
 </wm-nav>
 </div>
 </file>
 </example>
 */
