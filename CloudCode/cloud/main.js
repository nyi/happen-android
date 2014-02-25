
// Use Parse.Cloud.define to define as many cloud functions as you want.
// For example:
Parse.Cloud.define("hello", function(request, response) {
  response.success("Hello world!");
});
/*
Parse.Cloud.beforeSave(Parse.User, function(request, response) {
  

	var query = new Parse.Query(Parse.User);
				
				query.equalTo("phone", request.object.get("phone"));
				query.first({
	  			success: function(object) {
	    			if (object) {
	      			response.error("A user with this phone already exists.");
	    			}	else {
	      			response.success();
	    			}	
	  			},
	  			error: function(error) {
	    			response.error("Could not validate uniqueness for this User's phone.");
	  			}
				});
	    });
			*/
Parse.Cloud.define("acceptFriendRequest", function(request, response) {
  Parse.Cloud.useMasterKey();
  var friendRequestId = request.params.friendRequest;
  var source, target, friendRequest;
  var FriendRequest = Parse.Object.extend("FriendRequest");
  var query = new Parse.Query(FriendRequest);
  query.include("source");
  query.include("target");
  query.equalTo("objectId", friendRequestId);
  query.find().then(function(sources){
  	friendRequest = sources[0];
  	source = friendRequest.get("source");
    target = friendRequest.get("target");
    var targetRelation = target.relation("friends");
    targetRelation.add(source);
    return target.save();
  }).then(function(targetSaved){
  	var sourceRelation = source.relation("friends");
    sourceRelation.add(target);
    return source.save();   
  	
  }).then(function(sourceSaved){
    friendRequest.destroy({});
  	return; 
  }).then(function(success){
  	response.success(1);
  }, function(error) {
  	console.log("error processing friend request");
  	console.log(error);
  	response.error();
  });

});
