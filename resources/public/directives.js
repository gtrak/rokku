var main = com.gtrak.rokku.main;

angular.module('myApp', [])
  .directive('btn', function() {
    return {
      scope: { icon: '@', 
               key: '@'},
      template: '<button id={{key}} class="btn span4" ng-controller="ButtonCtrl" ng-click="click(key)" ng-transclude><i class="{{icon}}"></i></button>',
      restrict: 'E',
      transclude: true
    }
  })
  .directive('group', function() {
    return {
      template: '<div class="btn-group row-fluid" ng-transclude></div>',
      restrict: 'E',
      transclude: true
    }
  })
  .controller('ButtonCtrl', ['$scope', function($scope) {
    $scope.click = function(key){
      main.button_press(key);
    };
  }])
;

