/*global WM, */
/*Directive for Colorpicker */

WM.module('wm.widgets.form').requires = WM.module('wm.widgets.form').requires.concat(['colorpicker.module']);
/*Registering the colorpicker widget as a module.*/
WM.module('wm.widgets.form')
    /*Saving the colorpicker widget template using the $templateCache service.*/
    .run(['$templateCache', function ($templateCache) {
        'use strict';
        /*Assigning the template to an identifier.*/
        $templateCache.put('template/widget/form/colorpicker.html',
            '<div class="input-group app-colorpicker" data-ng-show="show" init-widget has-model>' +
                '<input colorpicker colorpicker-parent="true" class="form-control app-textbox" ' +
                ' data-ng-disabled="readonly || disabled"' +
                ' data-ng-required="required"' +
                ' data-ng-model="_model_"' +
                ' data-ng-change="_onChange({$event: $event, $scope: this})"' +
                '><span class="input-group-addon" data-ng-style="{backgroundColor:_model_}">&nbsp;</span></div>');
    }])
    /*Colorpicker widget directive definition*/
    .directive('wmColorpicker', ['PropertiesFactory', '$templateCache', 'WidgetUtilService', function (PropertiesFactory, $templateCache, WidgetUtilService) {
        'use strict';
        /*Obtaining the widget properties from the Base configurations, by combining widget properties and it's parent group's properties.*/
        var widgetProps = PropertiesFactory.getPropertiesOf('wm.colorpicker', ['wm.base', 'wm.base.events', 'wm.base.events.focus', 'wm.base.events.change']);

        return {
            'restrict': 'E',
            'scope': {},
            'replace': true,
            'template': function (tElement, tAttrs) {
                var isWidgetInsideCanvas = tAttrs.hasOwnProperty('widgetid'),
                    template = WM.element($templateCache.get('template/widget/form/colorpicker.html')),
                    target = template.children().first();

                /*setting the color picker widget name attribute to the inner input element*/
                template.find('input').attr('name', tAttrs.name);

                if (!isWidgetInsideCanvas) {
                    if (tAttrs.hasOwnProperty('onClick')) {
                        target.attr('data-ng-click', 'onClick({$event: $event, $scope: this})');
                    }

                    if (tAttrs.hasOwnProperty('onMouseenter')) {
                        target.attr('data-ng-mouseenter', 'onMouseenter({$event: $event, $scope: this})');
                    }

                    if (tAttrs.hasOwnProperty('onMouseleave')) {
                        target.attr('data-ng-mouseleave', 'onMouseleave({$event: $event, $scope: this})');
                    }

                    if (tAttrs.hasOwnProperty('onFocus')) {
                        target.attr('data-ng-focus', 'onFocus({$event: $event, $scope: this})');
                    }

                    if (tAttrs.hasOwnProperty('onBlur')) {
                        target.attr('data-ng-blur', 'onBlur({$event: $event, $scope: this})');
                    }
                }
                return template[0].outerHTML;
            },
            'compile': function () {
                return {
                    'pre': function (scope) {
                        /*Assigning the obtained widgetProps to the widget scope.*/
                        scope.widgetProps = widgetProps;
                    },
                    'post': function (scope, element, attrs) {

                        /*Executing WidgetUtilService method to initialize the widget with the essential configurations needed.*/
                        WidgetUtilService.postWidgetCreate(scope, element, attrs);
                    }
                };
            }
        };

    }]);

/**
 * @ngdoc directive
 * @name wm.widgets.form.directive:wmColorpicker
 * @restrict E
 *
 * @description
 * The `wmColorpicker` directive defines the colorpicker widget.
 *
 * @scope
 *
 * @requires PropertiesFactory
 * @requires $templateCache
 * @requires WidgetUtilService
 *
 * @param {string=} name
 *                  Name of the colorpicker widget.
 * @param {string=} placeholder
 *                  Placeholder text for the widget. <br>
 * @param {string=} scopedatavalue
 *                  This property accepts the value for the colorpicker widget from a variable defined in the script workspace. <br>
 *                  The scope variable is updated whenever there is a change in the colorpicker value.
 * @param {string=} datavalue
 *                  Value of the colorpicker widget. Accepts the value from a studio variable or from another widget's value.<br>
 *                  This property is bindable.
 * @param {boolean=} show
 *                  Show is a bindable property. <br>
 *                  This property will be used to show/hide the colorpicker widget on the web page. <br>
 *                  Default value: `true`. <br>
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
 *               <div style="width:100px; height:100px; background-color: {{color}}">
 *               </div><br>
 *
 *               <div>colorpicker value: <span style="font-weight: bold; color: {{color}};">{{color}}</span></div><br>
 *
 *               <div style="font-weight: bold; color: {{color}};">Hello there. Change the way I look using the colorpicker widget.</div><br>
 *
 *               <wm-colorpicker
 *                   scopedatavalue="color"
 *                   on-click="f('click');"
 *                   on-change="f('change');"
 *                   on-focus="f('focus');"
 *                   on-blur="f('blur');"
 *                   on-mouseenter="f('mouseenter');"
 *                   on-mouseleave="f('mouseleave')">
 *               </wm-colorpicker><br>
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
 *
 *              $scope.color = "blue";
 *
 *              $scope.f = function (eventtype) {
 *                  $scope[eventtype + 'Count']++;
 *              }
 *           }
 *       </file>
 *   </example>
 */
