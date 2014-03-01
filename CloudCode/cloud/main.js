
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
  //to allow edit of other users
  Parse.Cloud.useMasterKey();

  var friendRequestId = request.params.friendRequest;
  var source, target, friendRequest;

  //query for request
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

Parse.Cloud.define("sendFriendRequest", function(request, response) {
  
  var source = request.params.source;
  var targetUsername = request.params.target;
  var sourceObj, targetObj;
  sourceObj = Parse.User.current();
  var query = new Parse.Query(Parse.User);
  var error=false;
  query.equalTo("username", targetUsername);
  query.find().then(function(trgt) {
      if(trgt.length==0) {
        response.error("User not found.");
        error=true;
      }
      else {
        targetObj = trgt[0];
        var relation = trgt[0].relation("friends");
        var friendQuery = relation.query();
        friendQuery.equalTo("objectId", source);
        return friendQuery.find();
      }
  }).then(function(friends) {
      if(!error && friends.length>0) {
        response.error("You are already friends!");
        error=true;
      }
      else if(!error){
        var FriendRequest = Parse.Object.extend("FriendRequest");
        var newRequest = new FriendRequest();
        newRequest.set("source", sourceObj);
        newRequest.set("target", targetObj);
        return newRequest.save();
      }
  }).then(function(requestSaved) {
      if(!error) {
        var News = Parse.Object.extend("News");
        var newNews = new News();
        newNews.set("source", sourceObj);
        newNews.set("target", targetObj);
        newNews.set("type", SENT_REQUEST);
        newnews.set("isUnread", true);
        return newNews.save();
      }
  }).then(function(newsSaved) {
    if(!error)
      response.success("Saved news and request.");
  }, function(error) {
    response.error();
  });
});
