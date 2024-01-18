package edu.cmu.cc.minisite;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.logging.Logger;


/**
 * In this task you will populate a user's timeline.
 * This task helps you understand the concept of fan-out. 
 * Practice writing complex fan-out queries that span multiple databases.
 *
 * Task 4 (1):
 * Get the name and profile of the user as you did in Task 1
 * Put them as fields in the result JSON object
 *
 * Task 4 (2);
 * Get the follower name and profiles as you did in Task 2
 * Put them in the result JSON object as one array
 *
 * Task 4 (3):
 * From the user's followees, get the 30 most popular comments
 * and put them in the result JSON object as one JSON array.
 * (Remember to find their parent and grandparent)
 *
 * The posts should be sorted:
 * First by ups in descending order.
 * Break tie by the timestamp in descending order.
 */
public class TimelineServlet extends HttpServlet {
    private final static Logger LOGGER = Logger.getLogger(TimelineServlet.class.getName());

    /**
     * Your initialization code goes here.
     */
    public TimelineServlet() {
    }

    /**
     * Don't modify this method.
     *
     * @param request  the request object that is passed to the servlet
     * @param response the response object that the servlet
     *                 uses to return the headers to the client
     * @throws IOException      if an input or output error occurs
     * @throws ServletException if the request for the HEAD
     *                          could not be handled
     */
    @Override
    protected void doGet(final HttpServletRequest request,
                         final HttpServletResponse response) throws ServletException, IOException {

        // DON'T modify this method.
        String id = request.getParameter("id");
        String result = getTimeline(id);
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.print(result);
        writer.close();
    }

    /**
     * Method to get given user's timeline.
     *
     * @param id user id
     * @return timeline of this user
     */
    private String getTimeline(String id) {
        JsonObject result = new JsonObject();
        
        // Get the follower name and profiles
        JsonArray followers = getFollowers(id);
        getOrderedFollowers(followers, result, id);

        // From the user's followees, get the 30 most popular comments
        JsonArray popularComments = getPopularComments(id);
        if (popularComments != null && !popularComments.isJsonNull()) {
            result.add("comments", popularComments);
        } else {
            LOGGER.warning("Failed to fetch popular comments for user id: " + id);
        }

        // Get the name and profile of the user
        JsonObject userProfile = getProfile(id);
        if (userProfile != null) {
            result.addProperty("profile", userProfile.get("profile").getAsString());
            result.addProperty("name", userProfile.get("name").getAsString());
        } else {
            LOGGER.warning("Failed to fetch user profile for id: " + id);
        }
        
        return result.toString();
    }

    private JsonObject getProfile(String id) {
        JsonObject profile = null;
        try {
            profile = new ProfileServlet().getProfile(id);
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.severe("Error fetching profile: " + e.getMessage());
        }
        return profile;
    }

    private JsonArray getFollowers(String id) {
        JsonArray followers = null;
        try {
            followers = new FollowerServlet().getFollowers(id);
        } catch (Exception e) {
            LOGGER.severe("Error fetching followers: " + e.getMessage());
        }
        return followers;
    }

    private JsonArray getPopularComments(String id) {
        JsonArray comments = null;
        try {
            comments = new HomepageServlet().getComments(id);
        } catch (Exception e) {
            LOGGER.severe("Error fetching popular comments: " + e.getMessage());
        }
        return comments;
    }

    static protected void getOrderedFollowers(JsonArray followers, JsonObject result, String id) {
        JsonArray orderedFollowers = new JsonArray();
        if (followers != null && !followers.isJsonNull()) {
            for (JsonElement followerElement : followers) {
                JsonObject follower = followerElement.getAsJsonObject();
                
                // Create a new JsonObject to ensure the desired order
                JsonObject orderedFollower = new JsonObject();
                orderedFollower.addProperty("profile", follower.get("profile").getAsString());
                orderedFollower.addProperty("name", follower.get("name").getAsString());
                
                orderedFollowers.add(orderedFollower);
            }
            result.add("followers", orderedFollowers);
        } else {
            System.out.println("Failed to fetch followers for user id: " + id);
        }
    }

}

