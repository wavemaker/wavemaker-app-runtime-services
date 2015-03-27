/*global WM */
/*Directive for picture */

WM.module('wm.widgets.basic')
    .run(['$templateCache', '$rootScope', function ($templateCache, $rootScope) {
        'use strict';
        $templateCache.put('template/widget/picture.html',
                '<img init-widget alt="{{hint}}" title="{{hint}}" ng-class="[imgClass]" class="app-picture" data-ng-src="{{imagesource}}" ' +  $rootScope.getWidgetStyles() + ' data-ng-show="show">'
            );
    }])
    .directive('wmPicture', ['PropertiesFactory', 'WidgetUtilService', 'Utils', function (PropertiesFactory, WidgetUtilService, Utils) {
        'use strict';
        var widgetProps = PropertiesFactory.getPropertiesOf('wm.picture', ['wm.base', 'wm.base.editors', 'wm.base.events']),
            notifyFor = {
                'pictureaspect': true,
                'picturesource': true,
                'shape': true
            };

        /* Define the property change handler. This function will be triggered when there is a change in the widget property */
        function propertyChangeHandler(scope, element, key, newVal) {
            switch (key) {
            case 'pictureaspect':
                switch (newVal) {
                case 'None':
                    element.css({width: '', height: ''});
                    break;
                case 'H':
                    element.css({width: '100%', height: ''});
                    break;
                case 'V':
                    element.css({width: '', height: '100%'});
                    break;
                case 'Both':
                    element.css({width: '100%', height: '100%'});
                    break;
                }
                break;
            case 'picturesource':
                scope.imagesource = Utils.getImageUrl(newVal);
                break;
            case 'shape':
                scope.imgClass = "img-" + newVal;
                break;
            }

        }

        return {
            'restrict': 'E',
            'replace': true,
            'scope': {},
            'template': WidgetUtilService.getPreparedTemplate.bind(undefined, 'template/widget/picture.html'),
            'compile': function () {
                return {
                    'pre': function (scope) {
                        scope.widgetProps = widgetProps;
                    },
                    'post': function (scope, element, attrs) {

                        /* register the property change handler */
                        WidgetUtilService.registerPropertyChangeListener(propertyChangeHandler.bind(undefined, scope, element), scope, notifyFor);
                        WidgetUtilService.postWidgetCreate(scope, element, attrs);
                    }
                };
            }
        };
    }]);

/**
 * @ngdoc directive
 * @name wm.widgets.basic.directive:wmPicture
 * @restrict E
 *
 * @description
 * The `wmPicture` directive defines the picture widget.
 *
 * @scope
 *
 * @requires PropertiesFactory
 * @requires WidgetUtilService
 * @requires Utils
 *
 * @param {string=} name
 *                  Name of the picture widget.
 * @param {string=} hint
 *                  Title/hint for the picture. <br>
 *                  This property is bindable.
 * @param {string=} width
 *                  Width of the picture.
 * @param {string=} height
 *                  Height of the picture.
 * @param {string=} pictureaspect
 *                  This property can automatically size an image to the height or width of the picture widget. <br>
 *                  Valid values are: <br>
 *                      `None`: the image is displayed at its default size. <br>
 *                      `H`: image is resized so that the width of the image is the same as the width of the picture widget. <br>
 *                      `V`: image is resized so that the height of the image is the same as the height of the picture widget. <br>
 *                      `Both`: image is resized so that the height and width of the image are same as the height and width of the picture widget. <br>
 *                  Default value is: `None`
 * @param {string=} picturesource
 *                  This property specifies the source for the picture. <br>
 *                  This property is bindable. <br>
 *                  Default value is: `resources/images/imagelists/default-image.png`.
 * @param {boolean=} show
 *                  This property will be used to show/hide the picture widget on the web page. <br>
 *                  Show is a bindable property. <br>
 *                  Default value: `true`.
 * @param {boolean=} disabled
 *                  This property will be used to disable/enable the picture widget on the web page. <br>
 *                  Disabled is a bindable property. <br>
 *                  Default value: `false`.
 * @param {string=} on-click
 *                  Callback function for `click` event.
 * @param {string=} on-mouseenter.
 *                  Callback function for `mouseenter` event.
 * @param {string=} on-mouseleave
 *                  Callback function for `mouseleave` event.
 * @example
 *   <example module="wmCore">
 *       <file name="index.html">
 *           <div data-ng-controller="Ctrl" class="wm-app">
 *               <div>single click count: {{clickCount}}</div>
 *               <div>mouse enter count: {{mouseenterCount}}</div>
 *               <div>mouse leave count: {{mouseleaveCount}}</div>
 *               <wm-picture picturesource="{{image}}" on-click="f('click')" on-mouseenter="f('mouseenter')"  on-mouseleave="f('mouseleave')"></wm-picture>
 *               <wm-composite>
 *                   <wm-label caption="Select an image:"></wm-label>
 *                   <wm-select scopedatavalue="image" scopedataset="images"></wm-select>
 *               </wm-composite>
 *           </div>
 *       </file>
 *       <file name="script.js">
 *          function Ctrl($scope) {
 *              $scope.clickCount =
 *              $scope.mouseenterCount =
 *              $scope.mouseleaveCount = 0;
 *
 *              $scope.images = {
 *                  "http://angularjs.org/img/AngularJS-large.png": "angularJS",
 *                  "http://c0179631.cdn.cloudfiles.rackspacecloud.com/wavemaker_logo1.jpg": "wavemaker"
 *              };
 *
 *              $scope.f = function (eventtype) {
 *                  $scope[eventtype + 'Count']++;
 *              }
 *           }
 *       </file>
 *   </example>
 */