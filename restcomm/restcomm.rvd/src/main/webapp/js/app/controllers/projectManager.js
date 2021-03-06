App.controller('projectManagerCtrl', function ( $scope, $http, $location, $stateParams, $timeout, $upload, notifications, authentication) {

	var account = authentication.getAccount();

	$scope.authInfo = authentication.getAuthInfo();
	$scope.projectNameValidator = /^[^:;@#!$%^&*()+|~=`{}\\\[\]"<>?,\/]+$/;
	$scope.projectKind = $stateParams.projectKind;
	if ( $scope.projectKind != 'voice' && $scope.projectKind != 'ussd' && $scope.projectKind != 'sms')
		$scope.projectKind = 'voice';
	$scope.error = undefined; 
	//$scope.notifications = [];

	
	$scope.refreshProjectList = function() {
		var restcommApps;
		var projectList = [];
		$http({
			url: '/restcomm/2012-04-24/Accounts/' + account.sid + '/Applications.json',
			method: 'GET',
			headers: {Authorization: authentication.getAuthHeader()}
		}).success(function (data, status, headers, config) {
			restcommApps = data;
			$http({url: 'services/projects',
				method: "GET"
			}).success(function (data, status, headers, config) {
				for (var i in restcommApps) {
					var currentApp = restcommApps[i];
					var applicationSid = currentApp.sid;
					for ( var i=0; i < data.length; i ++){
						if(data[i].name === applicationSid){
							var project = {};
							project.applicationSid = applicationSid;
							project.name = currentApp.friendly_name;
							project.startUrl = currentApp.rcml_url;
							project.kind = currentApp.kind;
							project.viewMode = 'view';
							projectList.push(project);
							break;
						}
					}
				}
				$scope.projectList = projectList;
			}).error(function (data, status, headers, config) {
				if (status == 500)
				notifications.put({type:'danger',message:"Internal server error"});
			});
		}).error(function (data, status, headers, config) {
			if (status == 500)
				notifications.put({type:'danger',message:"Internal server error"});
			if (status == 401){
				notifications.put({type:'danger',message:"You have been logged out! Please, log-in using the same Account in RVD and Adminitration console.",timeout:0});
				authentication.doLogout();
			}
		});
	}
	
	$scope.createNewProject = function(name, kind, ticket) {
		$http({url: 'services/projects/' + name + "/?kind=" + kind,
				method: "PUT"
		})
		.success(function (data, status, headers, config) {
			console.log( "project created");
			$location.path("/designer/" + data.sid + "=" + name);
		 })
		 .error(function (data, status, headers, config) {
			if (status == 409) {
				console.log("project already exists");
				notifications.put({type:'danger',message:'A Voice, SMS or USSD project  with that name already exists in the workspace (maybe it belongs to another user).'});
			} else
			if (status >= 500) {
				console.log("internal server error: " + status);
				notifications.put({type:'danger',message:'Internal server error.'});
			} else {
				console.log("operation failed: " + status);
				notifications.put({type:'danger',message:'Could not create project.'});
			}
		 });
	}
	
	
	$scope.editProjectName = function(projectItem) {
		projectItem.viewMode = 'edit';
		projectItem.newProjectName = projectItem.name;
		projectItem.errorMessage = "";
	}
	
	$scope.applyNewProjectName = function(projectItem, ticket) {
		if ( projectItem.name == projectItem.newProjectName ) {
			projectItem.viewMode = 'view';
			return;
		}
		$http({ method: "PUT", url: 'services/projects/' + projectItem.applicationSid + '/rename?newName=' + projectItem.newProjectName})
			.success(function (data, status, headers, config) { 
				console.log( "project " + projectItem.name + " renamed to " + projectItem.newProjectName );
				projectItem.name = projectItem.newProjectName;
				projectItem.viewMode = 'view';
				
			})
			.error(function (data, status, headers, config) {
				if (status == 409)
					projectItem.errorMessage = "Project already exists!";
				else
					projectItem.errorMessage = "Cannot rename project";
			});
	}
	
	$scope.deleteProject = function(projectItem, ticket) {
		$http({ method: "DELETE", url: 'services/projects/' + projectItem.applicationSid})
		.success(function (data, status, headers, config) { 
			console.log( "project " + projectItem.name + " deleted " );
			$scope.refreshProjectList();
			projectItem.showConfirmation = false;
		})
		.error(function (data, status, headers, config) {
		    console.log("cannot delete project");
		    if (status >= 500) {
		        notifications.put({type:'danger',message:'Internal server error.'});
		    } else {
		        notifications.put({type:'danger',message:'Could not delete project.'});
		    }
		});
	}
	
	$scope.onFileSelect_ImportProject = function($files, ticket) {
	    for (var i = 0; i < $files.length; i++) {
	      var file = $files[i];
	      $scope.upload = $upload.upload({
	        url: 'services/projects',
	        file: file,
	      }).progress(function(evt) {
	        //console.log('percent: ' + parseInt(100.0 * evt.loaded / evt.total));
	      }).success(function(data, status, headers, config) {
	    	  console.log('Project imported successfully');
	    	  $scope.refreshProjectList();
	    	  notifications.put({message:"Project imported successfully", type:"success"});
	      }).error(function(data, status, headers, config) {
	    	  if (status == 400 && data && data.exception && data.exception.className == "UnsupportedProjectVersion") {
	    		  console.log(data.exception.message);
	    		  notifications.put({message:"Cannot import project. " + data.exception.message, type:"danger"});
	    	  } else {
                  console.log(data);
	    	      notifications.put({message:"Error importing project", type:"danger"});
	    	  }
	      });
	    }
	};
	
    $scope.refreshProjectList();	
	
});

