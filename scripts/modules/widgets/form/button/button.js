/*global WM */
/*Directive for button */

WM.module('wm.widgets.form')
    .run(['$templateCache', '$rootScope', function ($templateCache, $rootScope) {
        'use strict';

        $templateCache.put('template/widget/form/button.html',
            '<button class="btn btn-default app-button" init-widget title="{{hint}}" ' + $rootScope.getWidgetStyles() +
                ' data-ng-disabled="disabled" data-ng-show="show">' +
                '<img data-identifier="img" class="button-image-icon" data-ng-src="{{iconsrc}}"  data-ng-show="showimage" data-ng-style="{width:iconwidth ,height:iconheight, margin:iconmargin}"/>' +
                '<i class="{{iconclass}}" data-ng-style="{width:iconwidth, height:iconheight, margin:iconmargin, fontSize:iconwidth}" data-ng-show="showicon"></i> ' +
                '<span class="btn-caption"></span>' +
                '<span class="badge">{{badgevalue}}</span>' +
            '</button>'
            );
    }])
    .directive('wmButton', ['PropertiesFactory', 'WidgetUtilService', '$sce', 'Utils', function (PropertiesFactory, WidgetUtilService, $sce, Utils) {
        'use strict';

        var widgetProps = PropertiesFactory.getPropertiesOf('wm.button', ['wm.base', 'wm.base.editors', 'wm.base.events', 'wm.base.events.focus']),
            notifyFor = {
                'iconclass': true,
                'iconurl': true,
                'caption': true
            };

        /* Define the property change handler. This function will be triggered when there is a change in the widget property */
        function propertyChangeHandler(scope, element, key, newVal) {
            switch (key) {
            case 'iconclass':
                /*showing icon when iconurl is not set*/
                scope.showicon = scope.iconclass !== '_none_' && newVal !== '' && !scope.iconurl;
                break;
            case 'iconurl':
                /*hiding icon when iconurl is set*/
                /*showing icon when iconurl is not set*/
                var showIcon = newVal === '';
                scope.showicon = showIcon;
                scope.showimage = !showIcon;
                scope.iconsrc = Utils.getImageUrl(newVal);
                break;
            case 'caption':
                if (WM.isObject(newVal)) {
                    element.children('.btn-caption').text(JSON.stringify(newVal));
                } else {
                    element.children('.btn-caption').html(($sce.trustAs($sce.HTML, newVal.toString()).toString()));
                }
                break;
            }
        }

        return {
            'restrict': 'E',
            'replace': true,
            'scope': {},
            'template': WidgetUtilService.getPreparedTemplate.bind(undefined, 'template/widget/form/button.html'),
            'compile': function (tElement) {
                return {
                    'pre': function (scope, element, attrs) {
                        //@Deprecated iconname; use iconclass instead
                        if (!attrs.iconclass && attrs.iconname) {
                            WM.element(tElement.context).attr('iconclass', 'glyphicon glyphicon-' + attrs.iconname);
                            attrs.iconclass = 'glyphicon glyphicon-' + attrs.iconname;
                        }
                        /*Applying widget properties to directive scope*/
                        scope.widgetProps = widgetProps;
                    },
                    'post': function (scope, element, attrs) {
                        WidgetUtilService.registerPropertyChangeListener(propertyChangeHandler.bind(undefined, scope, element), scope, notifyFor);
                        WidgetUtilService.postWidgetCreate(scope, element, attrs);
                    }
                };
            }
        };
    }]);

/**
 * @ngdoc directive
 * @name wm.widgets.form.directive:wmButton
 * @restrict E
 *
 * @description
 * The `wmButton` directive defines the button widget.
 *
 * @scope
 *
 * @requires PropertiesFactory
 * @requires WidgetUtilService
 * @requires $sce
 *
 * @param {string=} name
 *                  Name of the button widget.
 * @param {string=} type
 *                  Type of the button widget. <br>
 *                  valid value is: button/submit/reset <br>
 *                  Default value: `button`
 * @param {string=} hint
 *                  Title/hint for the button. <br>
 *                  This property is bindable.
 * @param {string=} caption
 *                  Content of the button. <br>
 *                  This property is bindable.
 * @param {string=} width
 *                  Width of the button.
 * @param {string=} height
 *                  Height of the button.
 * @param {boolean=} show
 *                  Show is a bindable property. <br>
 *                  This property will be used to show/hide the button widget on the web page. <br>
 *                  Default value: `true`. <br>
 * @param {boolean=} disabled
 *                  Disabled is a bindable property. <br>
 *                  This property will be used to disable/enable the button widget on the web page. <br>
 *                  Default value: `false`. <br>
 * @param {string=} iconclass
 *                  CSS class of the icon.
 * @param {string=} iconurl
 *                  url of the icon.
 * @param {string=} iconwidth
 *                  width of the icon.
 *                  Default value: 16px
 * @param {string=} iconheight
 *                  height of the icon.
 *                  Default value: 16px
 * @param {string=} iconmargin
 *                  margin of the icon.
 * @param {string=} on-click
 *                  Callback function for `click` event.
 * @param {string=} on-dblclick
 *                  Callback function for `dblclick` event.
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
 *               <div>dbl click count: {{dblclickCount}}</div>
 *               <div>mouse enter count: {{mouseenterCount}}</div>
 *               <div>mouse leave count: {{mouseleaveCount}}</div>
 *               <div>focus count: {{focusCount}}</div>
 *               <div>blur count: {{blurCount}}</div>
 *               <wm-button
 *                   caption="{{caption}}"
 *                   hint="hint/title for button"
 *                   on-click="f('click');"
 *                   on-dblclick="f('dblclick');"
 *                   on-focus="f('focus');"
 *                   on-blur="f('blur');"
 *                   on-mouseenter="f('mouseenter');"
 *                   on-mouseleave="f('mouseleave')"
 *                   width="{{width}}"
 *                   height="{{height}}"
 *                   color="{{color}}"
 *                   iconclass="{{icon}}">
 *               </wm-button><br>
 *               <wm-composite>
 *                   <wm-label caption="caption:"></wm-label>
 *                   <wm-text scopedatavalue="caption"></wm-text>
 *               </wm-composite>
 *               <wm-composite>
 *                   <wm-label caption="width:"></wm-label>
 *                   <wm-text scopedatavalue="width"></wm-text>
 *               </wm-composite>
 *               <wm-composite>
 *                   <wm-label caption="height:"></wm-label>
 *                   <wm-text scopedatavalue="height"></wm-text>
 *               </wm-composite>
 *               <wm-composite>
 *                   <wm-label caption="color:"></wm-label>
 *                   <wm-select scopedatavalue="color" scopedataset="colors"></wm-select>
 *               </wm-composite>
 *               <wm-composite>
 *                   <wm-label caption="icon:"></wm-label>
 *                   <wm-select scopedatavalue="icon" scopedataset="icons"></wm-select>
 *               </wm-composite>
 *           </div>
 *       </file>
 *       <file name="script.js">
 *          function Ctrl($scope) {
 *              $scope.clickCount =
 *              $scope.dblclickCount =
 *              $scope.mouseenterCount =
 *              $scope.mouseleaveCount =
 *              $scope.focusCount =
 *              $scope.blurCount = 0;
 *
 *              $scope.width = "100px";
 *              $scope.height= "30px";
 *              $scope.caption = " Click Me! ";
 *              $scope.color = "crimson";
 *
 *              $scope.icons = ["ok", "star", "remove", "user", "random"];
 *              $scope.colors = ["crimson", "green", "orange", "red"];
 *
 *              $scope.f = function (eventtype) {
 *                  $scope[eventtype + 'Count']++;
 *              }
 *           }
 *       </file>
 *   </example>
 */

