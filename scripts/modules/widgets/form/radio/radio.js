/*global WM*/
/*Directive for Radio */

WM.module('wm.widgets.form')
    .run(['$templateCache', '$rootScope', function ($templateCache, $rootScope) {
        'use strict';
        $templateCache.put('template/widget/form/radio.html',
            '<div class="radio app-radio" init-widget has-model data-ng-show="show" title="{{hint}}">' +
                '<label ' + $rootScope.getWidgetStyles() + '>' +
                    '<input type="radio" class="app-radiobutton"' +
                        ' value="{{checkedvalue}}"' +
                        ' data-ng-model="_model_"' + /* _model_ is a private variable inside this scope */
                        ' data-ng-readonly="readonly" ' +
                        ' data-ng-required="required" ' +
                        ' data-ng-disabled="disabled" ' +
                        ' data-ng-change="_onChange({$event: $event, $scope: this})" ' + /* private method defined in this scope */
                    '/>' +
                    '{{caption || "&nbsp;"}}' +
                '</label>' +
            '</div>'
            );
    }])
    .directive('wmRadio', ['PropertiesFactory', 'WidgetUtilService', '$templateCache', function (PropertiesFactory, WidgetUtilService, $templateCache) {
        'use strict';
        /*Obtaining properties specific to radio widget by extending from all editor related widget properties*/
        var widgetProps = PropertiesFactory.getPropertiesOf('wm.radio', ['wm.base', 'wm.base.editors', 'wm.base.editors.abstracteditors']),
            notifyFor = {
                'height': true,
                'radiogroup': true
            };

        /* Define the property change handler. This function will be triggered when there is a change in the widget property */
        function propertyChangeHandler(radtioBtn, key, newVal) {
            switch (key) {
            /*case 'height':
                scope.lineheight = (scope.height === 'undefined' || scope.height === '') ? '24px' : (parseInt(scope.height, 10) - (parseInt(scope.bordertop, 10) + parseInt(scope.borderbottom, 10) + parseInt(scope.paddingtop, 10) + parseInt(scope.paddingbottom, 10))) + 'px';
                break;*/
            case 'radiogroup':
                radtioBtn.attr('name', newVal);
                break;
            }
        }

        return {
            'restrict': 'E',
            'scope': {},
            'replace': true,
            'template': function (tElement, tAttrs) {
                var template = WM.element($templateCache.get('template/widget/form/radio.html')),
                    radioBtn,
                    isWidgetInsideCanvas = tAttrs.hasOwnProperty('widgetid');
                radioBtn = template.find('input[type=radio]');

                if (!isWidgetInsideCanvas) {
                    if (tAttrs.hasOwnProperty('onClick')) {
                        template.attr('data-ng-click', 'onClick({$event: $event, $scope: this})');
                    }

                    if (tAttrs.hasOwnProperty('onMouseenter')) {
                        template.attr('data-ng-mouseenter', 'onMouseenter({$event: $event, $scope: this})');
                    }

                    if (tAttrs.hasOwnProperty('onMouseleave')) {
                        template.attr('data-ng-mouseleave', 'onMouseleave({$event: $event, $scope: this})');
                    }

                    if (tAttrs.hasOwnProperty('onFocus')) {
                        radioBtn.attr('data-ng-focus', 'onFocus({$event: $event, $scope: this})');
                    }

                    if (tAttrs.hasOwnProperty('onBlur')) {
                        radioBtn.attr('data-ng-blur', 'onBlur({$event: $event, $scope: this})');
                    }
                }
                /*Set name for the model-holder, to ease submitting a form*/
                radioBtn.attr('name', tAttrs.name);

                return template[0].outerHTML;
            },
            'compile': function () {
                return {
                    'pre': function (scope) {
                        scope.widgetProps = widgetProps;
                    },
                    'post': function (scope, element, attrs) {

                        /* register the property change handler */
                        var radtioBtn = element.find('input[type=radio]');
                        WidgetUtilService.registerPropertyChangeListener(propertyChangeHandler.bind(undefined, radtioBtn), scope, notifyFor);

                        WidgetUtilService.postWidgetCreate(scope, element, attrs);
                    }
                };
            }
        };
    }]);

/**
 * @ngdoc directive
 * @name wm.widgets.form.directive:wmRadio
 * @restrict E
 *
 * @description
 * The `wmRadio` directive defines the radio widget.
 *
 * @scope
 *
 * @requires PropertiesFactory
 * @requires WidgetUtilService
 *
 * @param {string=} name
 *                  Name of the radio widget.
 * @param {string=} hint
 *                  Title/hint for the radio. <br>
 *                  This property is bindable.
 * @param {string=} width
 *                  Width of the radio.
 * @param {string=} height
 *                  Height of the radio.
 * @param {boolean=} show
 *                  Show is a bindable property. <br>
 *                  This property will be used to show/hide the radio widget on the web page. <br>
 *                  Default value: `true`. <br>
 * @param {boolean=} disabled
 *                  Disabled is a bindable property. <br>
 *                  This property will be used to disable/enable the radio widget on the web page. <br>
 *                  Default value: `false`. <br>
 * @param {boolean=} required
 *                  This property will be used to validate the state of the radio widget when used inside a form widget.
 * @param {string=} checkedvalue
 *                  This property defines the value of the checkbox widget when the element is in the checked state. Mandatory for displaying widget value.<br>
 *                  Default value: `false`. <br>
 * @param {string=} scopedatavalue
 *                  This property accepts the value for the radio widget from a variable defined in the script workspace. <br>
 *                  The scope variable is updated whenever there is a change in the radio value.
 * @param {string=} radiogroup
 *                  This property allows you to assign several radioButton widgets to the same group. <br>
 *                  The radiogroup property will be the same for all the radioButtons that have the same radiogroup property value.
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
 *               <wm-composite>
 *                   <wm-label caption="{{rad1caption}}"></wm-label>
 *                   <wm-radio
 *                       hint="hint/title for radio"
 *                       radiogroup="{{radiogroup}}"
 *                       checkedvalue="Yes"
 *                       scopedatavalue="selectedvalue"
 *                       on-click="f('click');"
 *                       on-change="f('change');"
 *                       on-focus="f('focus');"
 *                       on-blur="f('blur');"
 *                       on-mouseenter="f('mouseenter');"
 *                       on-mouseleave="f('mouseleave')"
 *                       width="{{width}}"
 *                       height="{{height}}">
 *               </wm-composite>
 *               <wm-composite>
 *                   <wm-label caption="{{rad2caption}}"></wm-label>
 *                   <wm-radio
 *                       hint="hint/title for radio"
 *                       radiogroup="{{radiogroup}}"
 *                       checkedvalue="No"
 *                       scopedatavalue="selectedvalue"
 *                       on-click="f('click');"
 *                       on-change="f('change');"
 *                       on-focus="f('focus');"
 *                       on-blur="f('blur');"
 *                       on-mouseenter="f('mouseenter');"
 *                       on-mouseleave="f('mouseleave')"
 *                       width="{{width}}"
 *                       height="{{height}}">
 *               </wm-composite>
 *               <br>
 *
 *               <div> Selected Value: <span style="font-weight: bold;">{{selectedvalue}}</span></div>
 *
 *               <wm-composite>
 *                   <wm-label caption="Radio1 Caption:"></wm-label>
 *                   <wm-text scopedatavalue="rad1caption"></wm-text>
 *               </wm-composite>
 *               <wm-composite>
 *                   <wm-label caption="Radio2 Caption:"></wm-label>
 *                   <wm-text scopedatavalue="rad2caption"></wm-text>
 *               </wm-composite>
 *               <wm-composite>
 *                   <wm-label caption="Radio Group:"></wm-label>
 *                   <wm-text scopedatavalue="radiogroup"></wm-text>
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
 *
 *              $scope.rad1caption = "MALE";
 *              $scope.rad2caption = "FEMALE";
 *
 *              $scope.radiogroup = "gender";
 *
 *              $scope.width = "50px";
 *              $scope.height= "20px";
 *
 *              $scope.f = function (eventtype) {
 *                  $scope[eventtype + 'Count']++;
 *              }
 *           }
 *       </file>
 *   </example>
 */
