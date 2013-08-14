var main = com.gtrak.rokku.main;

angular.module('myApp', [])
  .directive('btn', function() {
    return {
      controller: ['$scope','state', function($scope,state) {
        $scope.click = function(key){
          main.button_press(key);
        };
        $scope.state = state;
      }],
      scope: { icon: '@', 
               key: '@'},
      template: '<button id={{key}} class="btn span4" ng-class="{active: key==state.currentKey}" ng-click="click(key)" ng-transclude><i class="{{icon}}"></i></button>',
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
  .factory('state', function() {
    return {currentKey:null}
  })
  .controller('cljs',function($scope){
    $scope.main = main;
  })
  .controller('CurrentButton',['$scope','state',function($scope,state){
    $scope.state = state;
  }])

;

