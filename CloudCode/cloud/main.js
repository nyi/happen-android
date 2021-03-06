
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
    //add source to target relation
    targetRelation.add(source);
    return target.save();
  }).then(function(targetSaved){
  	var sourceRelation = source.relation("friends");
  	//add target to source relation
    sourceRelation.add(target);
    return source.save();
  }).then(function(sourceSaved){
    //remove request from database
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
        newNews.set("type", "SENT_REQUEST");
        newNews.set("isUnread", true);
        return newNews.save();
      }
  }).then(function(newsSaved) {
    if(!error)
      response.success("Saved news and request.");
  }, function(error) {
    response.error();
  });
});


Parse.Cloud.define("deleteEvent", function(event, response) {
  var Event = Parse.Object.extend("Event");
  var eventId = event.params.eventId;
  var event = new Event();
  event.id = eventId;
  var newsQuery = new Parse.Query("News");
 
  newsQuery.equalTo("event", event);

  var error = false;

//delete news
  newsQuery.find().then(function(news) {
    //if any news are found delete
    if(news!=null && news.length>0) 
    {
      for (var i = news.length - 1; i >= 0; i--) {
        news[i].destroy({});
      };
    }

    //setup query for actual event
    var eventQuery = new Parse.Query("Event");
    eventQuery.equalTo("objectId", eventId);
    return eventQuery.find();
    
  }).then(function(eventresult) {  // then delete event itself
    if(eventresult == null || eventresult.length==0)
    {
      error = true;
      response.error("Could not find event");
    }
    else {
      eventresult[0].destroy({});
    }
    return;
  }).then(function(done) {
    if(!error)
      response.success("deleted");
  }, function(error) {
    response.error("error deleting event");
  });
});


Parse.Cloud.define("meTooEvent", function(event, response) {
  var eventQuery = new Parse.Query("Event");
  var eventId = event.params.eventId;
  eventQuery.equalTo("objectId", eventId);
  var error = false;
  var user = Parse.User.current();
  var thisEvent;

//first find event
  eventQuery.find().then(function(eventFound) {

    thisEvent = eventFound[0];
    //add this user to me too array
    thisEvent.addUnique("MeToos", user);
    var meTooArray = thisEvent.get("MeToos");

    thisEvent.set("meTooCount", meTooArray.length);   
    return thisEvent.save();
  }).then( function(newscreate) {

    //now create news
    var News = Parse.Object.extend("News");
    var newsItem = new News();
    newsItem.set("source", user);
    newsItem.set("target", thisEvent.get("creator"));
    newsItem.set("isUnread", true);
    newsItem.set("type", "ME_TOO");
    newsItem.set("event", thisEvent);
    return newsItem.save();

  }).then( function(saved)
  {
      response.success("meToo'ed event successfully");
  }, function(error) {
      response.error("error me too-ing");
  });
});

Parse.Cloud.define("hideEvent", function(event, response) {
  var eventQuery = new Parse.Query("Event");
  var eventId = event.params.eventId;
  eventQuery.equalTo("objectId", eventId);
  var error = false;

  eventQuery.find().then(function(eventFound) {
    var user = Parse.User.current();
    var thisEvent = eventFound[0];
    thisEvent.addUnique("Hides", user);
    return thisEvent.save();
  }).then( function(saved) {
      response.success("hid event");
  }, function(error) {
      response.error("error hiding event");
  });
});

Parse.Cloud.define("undoMeTooEvent", function(event, response) {
  var eventQuery = new Parse.Query("Event");
  var eventId = event.params.eventId;
  eventQuery.equalTo("objectId", eventId);
  var error = false;

  var thisEvent, user;

  eventQuery.find().then(function(eventFound) {
    //find event
    user = Parse.User.current();
    thisEvent = eventFound[0];

    //remove user from event list of metoos
    thisEvent.remove("MeToos", user);

    //sync meTooCount
    var meTooArray = thisEvent.get("MeToos");
    thisEvent.set("meTooCount", meTooArray.length);   

    //save
    return thisEvent.save();
  }).then( function(deleteNews) {

    //querynews
    var newsQuery = new Parse.Query("News");
    newsQuery.equalTo("event", thisEvent);
    newsQuery.equalTo("source", user);
    return newsQuery.find();

  }).then( function(newsToDelete) {
    if(newsToDelete!=null && newsToDelete.length>0) 
    {
      for (var i = newsToDelete.length - 1; i >= 0; i--) {
        newsToDelete[i].destroy({});
      };
    }
  }).then (function(end) {
      response.success("undid meToo");
  }, function(error) {
      response.error("error undoing me-too-ing");
  });
});

Parse.Cloud.define("allNewsRead", function(news, response) {
  var newsQuery = new Parse.Query("News");
  var user = Parse.User.current();
  newsQuery.equalTo("target", user);
  newsQuery.equalTo("isUnread", true);
  var success = false;
  newsQuery.find().then (function(readNews) {
    if(readNews!=null)
    {
      for ( var i = readNews.length - 1; i >= 0; i--) {
        readNews[i].set("isUnread", false);
      };
      Parse.Object.saveAll(readNews, {
        success: function(list) {
          response.success("saved all successfully");
          success=true;
        },
        error: function(error) {
            // An error occurred while saving one of the objects.
          response.error("did NOT save all successfully");
        },
      }); 
    }
    else
    {
      response.success("no news to read");
    }


  }, function(error) {

  });
});















