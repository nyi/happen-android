
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
  var source_id = request.params.source;
  var target_id = request.params.target;
  console.log(source_id);
  console.log(target_id);
  var source, target;

  var query = new Parse.Query(Parse.User);
  query.equalTo("objectId", source_id);
  query.find().then(function(sources){
  	console.log("here1");
  	source = sources[0];
  	var query2 = new Parse.Query(Parse.User)
  	query2.equalTo("objectId", target_id);
  	return query2.find();

  }).then(function(targets){
  	console.log("here2");
  	target = targets[0];
  	var targetRelation = target.relation("friends");
  	targetRelation.add(source);
  	return target.save();
  	
  }).then(function(targetSaved){
  	console.log("here3");
  	var sourceRelation = source.relation("friends");
  	sourceRelation.add(target);
  	console.log("here4");
 	return source.save();	  
  }).then(function(sourceSaved){
  	response.success(1);
  }, function(error) {
  	console.log("error in cloud code");
  	console.log(error);
  	response.error();
  });

});
