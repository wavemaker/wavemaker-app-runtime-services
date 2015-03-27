/*global WM */
/*jslint sub: true*/
/*Directive for popover */

WM.module('wm.widgets.basic')
    .run(['$templateCache', '$rootScope', function ($templateCache, $rootScope) {
        'use strict';

        $templateCache.put('template/widget/basic/popover.html',
            '<div init-widget page-container class="app-popover {{showPopover ? \'open\' : \'\'}}" >'+
                '<a data-identifier="popover" class="app-popover-link" init-widget data-ng-show="show" title="{{hint}}" data-ng-click="showPopover = !showPopover; handlePopoverToggle();" ' +$rootScope.getWidgetStyles() + ' >' +
                    '<img data-identifier="img" class="anchor-image-icon" data-ng-src="{{iconsrc}}"  data-ng-show="showimage" data-ng-style="{width:iconwidth ,height:iconheight, margin:iconmargin}"/>' +
                    '<i class="{{iconclass}}" data-ng-style="{width:iconwidth, height:iconheight, margin:iconmargin}" data-ng-show="showicon"></i> ' +
                    '<span class="anchor-caption"></span>' +
                '</a>'+
                 '<div class="app-popover popover {{popoverplacement}}">'+
                    '<div class="arrow" data-ng-show="{{popoverarrow}}"></div>'+
                    '<div class="popover-content">'+
                        '<div page-container-target data-ng-show="show"></div>'+
                    '</div>'+
                 '</div>'+
            '</div>');
    }])
    .directive('wmPopover', ['PropertiesFactory', 'WidgetUtilService', '$sce', 'Utils', "CONSTANTS", function (PropertiesFactory, WidgetUtilService, $sce, Utils, CONSTANTS) {
        'use strict';

        var widgetProps = PropertiesFactory.getPropertiesOf('wm.popover', ['wm.base']),
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
                  element.find('a span.anchor-caption').text(JSON.stringify(newVal));
                } else {
                  element.find('a span.anchor-caption').html(($sce.trustAs($sce.HTML, newVal.toString()).toString()));
                }
                break;
            }
        }

        function buildPopoverToggleHandler(scope, element, config){
            var AutoClose = function(target){
                var self = this;
                this.enable = false;
                this.eventName  = 'click.autoclose' + new Date().getTime();
                this.target = target;
                this.start = function(onClose){
                    this.target.on(this.eventName, function(){
                        self.enable = false;
                    });
                    WM.element('body').on(this.eventName, function(){
                       if(self.enable && onClose){
                          self.stop();
                          onClose();
                       }
                       self.enable = true;
                    });
                };
                this.stop = function(){
                    target.off(self.eventName);
                    WM.element('body').off(self.eventName);
                };
            };
            var toggleHandler = new (function(){
                this.popover = element.find('>.popover:first');
                this.config = config;
                this.element = element;
                this.autoclose = this.config.enableAutoClose ? new AutoClose(this.popover) : false;
                this.handle = function(){
                    var self = this;
                    if(scope.showPopover){
                        this.setPopoverPosition();
                    }
                    this.popover.toggle();
                    if(this.autoclose) {
                        if(scope.showPopover){
                            this.autoclose.enable = false;
                            this.autoclose.start(function(){
                                self.popover.hide();
                                /*changing showPopover to false is not hiding the popover*/
                                scope.showPopover = false;
                            });
                        } else {
                            this.autoclose.stop();
                        }
                    }
                };
                this.getDimensions = function(ele){
                    return {
                        'width' : Math.abs(ele.width()),
                        'height' : Math.abs(ele.height())
                    }
                };
                this.setPopoverPosition = function(){
                    var popoverDims = this.getDimensions(this.popover),
                        arrow = this.element.find('.arrow'),
                        placement = this.config.placement,
                        arrowDims = false ? this.getDimensions(arrow) : {'width' : 0, height : 0},
                        documentDims = this.getDimensions(WM.element(document)),
                        targetDims = this.getDimensions(this.element),
                        targetPosition = this.element.position(),
                        tipOffset = {
                            'width' : -arrowDims.width/2,
                            'height' : -arrowDims.height/2
                        },
                        popoverPosition = {
                            'left' : targetPosition.left + tipOffset.width,
                            'top'  : targetPosition.top + tipOffset.height
                        };
                    arrow.removeClass('top bottom left right');
                    if(placement == 'left' || placement == 'right'){
                        if(placement == 'left'){
                            popoverPosition.left += (-1 * (popoverDims.width + arrowDims.width));
                        } else {
                            popoverPosition.left += targetDims.width + arrowDims.width;
                        }
                        if (this.element.offset().top + popoverDims.height <= documentDims.height){
                            arrow.addClass('top');
                        } else {
                            popoverPosition.top = targetPosition.top + targetDims.height - popoverDims.height;
                            arrow.addClass('bottom');
                        }
                    } else if(placement == 'top' || placement == 'bottom'){
                        if(placement == 'top'){
                            popoverPosition.top += (-1 * popoverDims.height);
                        } else {
                            popoverPosition.top += targetDims.height + arrowDims.height;
                        }
                        if (this.element.offset().left + popoverDims.width <= documentDims.width){
                            arrow.addClass('left');
                        } else {
                            popoverPosition.left = targetPosition.left + targetDims.width - popoverDims.width;
                            arrow.addClass('right');
                        }
                    }
                    this.popover.css(popoverPosition);
                };
            })();
            return function(){
                toggleHandler.handle();
            }
        }

        return {
            'restrict': 'E',
            'replace': true,
            'scope': {},
            'transclude': true,
            'template': WidgetUtilService.getPreparedTemplate.bind(undefined, 'template/widget/basic/popover.html'),
            'compile': function () {
                return {
                    'pre': function (scope) {
                        scope.showicon = !scope.iconurl;
                        scope.showPopover = false;
                        scope.widgetProps = widgetProps;
                    },
                    'post': function (scope, element, attrs) {
                        WidgetUtilService.registerPropertyChangeListener(propertyChangeHandler.bind(undefined, scope, element), scope, notifyFor);
                        WidgetUtilService.postWidgetCreate(scope, element, attrs);
                        var popoverConfig = {
                                        'enableAutoClose' : scope.popoverautoclose,
                                        'showArrow' : scope.popoverarrow,
                                        'placement' : scope.popoverplacement
                                        };
                        if(CONSTANTS.isRunMode){
                            scope.handlePopoverToggle = buildPopoverToggleHandler(scope, element, popoverConfig);
                        } else {
                            scope.handlePopoverToggle =  function(){};
                        }
                    }
                };
            }
        };
    }]);

/**
 * @ngdoc directive
 * @name wm.widgets.basic.directive:wmPopover
 * @restrict E
 *
 * @description
 * The `wmPopover` directive defines the popover widget.
 * It can be dragged and moved in the canvas.
 *
 * @scope
 *
 * @requires PropertiesFactory
 * @requires WidgetUtilService
 * @requires Utils
 *
 * @param {string=} name
 *                  Name of the popover.
 * @param {string=} hint
 *                  Title/hint for th anchor. It can be bound to a variable as well so that it takes the value of the variable.
 * @param {string=} caption
 *                  Content of the popover. It can be bound to a variable as well so that it takes the value of the variable.
 * @param {boolean=} show
 *                  Show is a bindable property. <br>
 *                  This property will be used to show/hide the anchor on the web page. <br>
 *                  Default value: `true`. <br>
 * @param {string=} iconclass
 *                  CSS class for the icon
 * @param {string=} popoverplacement
 *                  defines the position of the popover ['top', 'bottom', 'left', 'right']
 * @param {boolean=} popoverarrow
 *                  If set true, then a arrow pointer will be shown.
 * @param {boolean=} popoverautoclose
 *                  If set true, then a click on the document (except popover content) will automatically close the popover.
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
 * @example
 *   <example module="wmCore">
 *       <file name="index.html">
 *           <div data-ng-controller="Ctrl" class="wm-app">
 *               <wm-popover name="popover1"
 *                       caption="User"
 *                       content="dropdownMenu"
 *                       popoverautoclose="true"
 *                       popoverplacement="top"
 *                       popoverarrow="true"></wm-popover>
 *           </div>
 *       </file>
 *   </example>
 */