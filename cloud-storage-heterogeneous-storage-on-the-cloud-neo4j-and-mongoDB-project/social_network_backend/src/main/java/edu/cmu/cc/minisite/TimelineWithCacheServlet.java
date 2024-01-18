package edu.cmu.cc.minisite;

import com.amazonaws.services.dynamodbv2.xspec.S;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import edu.cmu.cc.minisite.TimelineServlet;
/**
 * In this task you will populate a user's timeline.
 * This task helps you understand the concept of fan-out and caching.
 * Practice writing complex fan-out queries that span multiple databases.
 * Also practice using caching mechanism to boost your backend!
 *
 * Task 5 (1):
 * Get the name and profile of the user as you did in Task 3
 * Put them as fields in the result JSON object
 *
 * Task 5 (2);
 * Get the follower name and profiles as you did in Task 4
 * Put them in the result JSON object as one array
 *
 * Task 5 (3):
 * From the user's followees, get the 30 most popular comments
 * and put them in the result JSON object as one JSON array.
 * (Remember to find their parent and grandparent)
 *
 * Task 5 (4):
 * Make sure your implementation can finish a request that is sent
 * before in a short time.
 *
 * The posts should be sorted:
 * First by ups in descending order.
 * Break tie by the timestamp in descending order.
 */
public class TimelineWithCacheServlet extends HttpServlet {

    /**
     * You need to use this variable to implement your caching
     * mechanism. Please see {@link Cache#put}, {@link Cache#get}.
     *
     */
    private static Cache cache = new Cache();

    /**
     * Your initialization code goes here.
     */
    public TimelineWithCacheServlet() {
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

        // DON'T modify this method
        String id = request.getParameter("id");
        String result = getTimeline(id);

        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.addHeader("CacheHit", String.valueOf(cache.get(id) != null));
        PrintWriter writer = response.getWriter();
        writer.print(result);
        writer.close();
    }

    /**
     * Method to get given user's timeline.
     * You are required to implement caching mechanism with
     * given cache variable.
     *
     * @param id user id
     * @return timeline of this user
     */
    private String getTimeline(String id) throws IOException {

        JsonObject result = new JsonObject();

        // Check cache first
        String cachedResult = cache.get(id);
        if (cachedResult != null) {
            return cachedResult;
        }

        // Get user profile
        JsonObject userProfile = getProfile(id);

        // Get number of followers
        int numFollowers = getFollowerCount(id);

        // For top users, cache the result
        if (numFollowers > 300) {

            // Get followers
            JsonArray followers = getFollowers(id);
            TimelineServlet.getOrderedFollowers(followers, result, id);

            // Get popular comments 
            JsonArray popularComments = getPopularComments(id);
            result.add("comments", popularComments);

            // Add user profile
            result.addProperty("profile", userProfile.get("profile").getAsString());
            result.addProperty("name", userProfile.get("name").getAsString());

            // Cache the result
            cache.put(id, result.toString());
            
            return result.toString();
      
        } else {
            // For normal users, don't cache

            // Get followers
            JsonArray followers = getFollowers(id);
            TimelineServlet.getOrderedFollowers(followers, result, id);
            // Get popular comments
            JsonArray popularComments = getPopularComments(id); 
            result.add("comments", popularComments);

            // Add user profile
            result.addProperty("profile", userProfile.get("profile").getAsString());
            result.addProperty("name", userProfile.get("name").getAsString());

            return result.toString();
        }
      
    }


    private JsonObject getProfile(String id) {
        JsonObject profile = null;
        try {
            profile = new ProfileServlet().getProfile(id);
        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("Error fetching profile: " + e.getMessage());
        }
        return profile;
    }

    private JsonArray getFollowers(String id) {
        JsonArray followers = null;
        try {
            followers = new FollowerServlet().getFollowers(id);
        } catch (Exception e) {
            System.out.println("Error fetching followers: " + e.getMessage());
        }
        return followers;
    }

    private int getFollowerCount(String id) {
        int followerCount = 0;
        try {
            followerCount = new FollowerServlet().getFollowerCount(id);
        } catch (Exception e) {
            System.out.println("Error fetching followers: " + e.getMessage());
        }
        return followerCount;
    }

    private JsonArray getPopularComments(String id) {
        JsonArray comments = null;
        try {
            comments = new HomepageServlet().getComments(id);
        } catch (Exception e) {
            System.out.println("Error fetching popular comments: " + e.getMessage());
        }
        return comments;
    }

}

