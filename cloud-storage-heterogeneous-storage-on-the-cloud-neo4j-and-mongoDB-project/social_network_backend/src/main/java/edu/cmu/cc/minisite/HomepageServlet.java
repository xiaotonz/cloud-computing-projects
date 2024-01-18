package edu.cmu.cc.minisite;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Sorts;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import org.neo4j.driver.v1.StatementResult;


/**
 * Task 3:
 * Implement your logic to return all the comments authored by this user.
 *
 * You should sort the comments by ups in descending order (from the largest to the smallest one).
 * If there is a tie in the ups, sort the comments in descending order by their timestamp.
 */
public class HomepageServlet extends HttpServlet {

    /**
     * The endpoint of the database.
     *
     * To avoid hardcoding credentials, use environment variables to include
     * the credentials.
     *
     * e.g., before running "mvn clean package exec:java" to start the server
     * run the following commands to set the environment variables.
     * export MONGO_HOST=...
     */
    private static final String MONGO_HOST = System.getenv("MONGO_HOST");
    /**
     * MongoDB server URL.
     */
    private static final String URL = "mongodb://" + MONGO_HOST + ":27017";
    /**
     * Database name.
     */
    private static final String DB_NAME = "reddit_db";
    /**
     * Collection name.
     */
    private static final String COLLECTION_NAME = "posts";
    /**
     * MongoDB connection.
     */
    private static MongoCollection<Document> collection;

    /**
     * Initialize the connection.
     */
    public HomepageServlet() {
        Objects.requireNonNull(MONGO_HOST);
        MongoClientURI connectionString = new MongoClientURI(URL);
        MongoClient mongoClient = new MongoClient(connectionString);
        MongoDatabase database = mongoClient.getDatabase(DB_NAME);
        collection = database.getCollection(COLLECTION_NAME);
        collection.createIndex(Indexes.compoundIndex(
            Indexes.ascending("uid"),
            Indexes.descending("ups"),
            Indexes.descending("timestamp")
        ));    
        collection.createIndex(Indexes.ascending("cid"));
        collection.createIndex(Indexes.ascending("parent_id"));
    }

    /**
     * Implement this method.
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

        JsonObject result = new JsonObject();
        String id = request.getParameter("id");
        JsonArray comments = new JsonArray();
        // Create a query to get all comments from users and sort them by ups and timestamp
        FindIterable<Document> iterable = collection.find(Filters.eq("uid", id))
            .sort(Sorts.orderBy(Sorts.descending("ups"), Sorts.descending("timestamp")))
            .projection(Projections.exclude("_id"));

        // Convert the result to JSON
        MongoCursor<Document> cursor = iterable.iterator();
        try {
            while (cursor.hasNext()) {
                JsonObject jsonObject = new JsonParser().parse(cursor.next().toJson()).getAsJsonObject();
                comments.add(jsonObject);
            }
        } finally {
            cursor.close();
        }

        result.add("comments", comments);

        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(result.toString());
        writer.close();
    }

    protected JsonArray getComments(String id) {
        JsonArray comments = new JsonArray();
        List<String> followees = null;
        try {
            followees = new FollowerServlet().getFollowees(id);
        } catch (Exception e) {
            System.out.println("Failed to get followees for user id: " + id);
        }
        FindIterable<Document> iterable = collection.find(Filters.in("uid", followees))
            .sort(Sorts.orderBy(Sorts.descending("ups"), Sorts.descending("timestamp")))
            .projection(Projections.exclude("_id"))
            .limit(30);
        
        MongoCursor<Document> cursor = iterable.iterator();
        try {
            while (cursor.hasNext()) {
                JsonObject commentObj = new JsonParser().parse(cursor.next().toJson()).getAsJsonObject();
                String parentId = commentObj.has("parent_id") ? commentObj.get("parent_id").getAsString() : null;
                if (parentId != null) {
                    Document parentCommentDoc = collection.find(Filters.eq("cid", parentId))
                                                         .projection(Projections.exclude("_id"))
                                                         .first();
                    if (parentCommentDoc != null) {
                        JsonObject parentCommentObj = new JsonParser().parse(parentCommentDoc.toJson()).getAsJsonObject();
                        commentObj.add("parent", parentCommentObj);
        
                        String grandParentId = parentCommentObj.has("parent_id") ? parentCommentObj.get("parent_id").getAsString() : null;
                        if (grandParentId != null) {
                            Document grandParentCommentDoc = collection.find(Filters.eq("cid", grandParentId))
                                                                      .projection(Projections.exclude("_id"))
                                                                      .first();
                            if (grandParentCommentDoc != null) {
                                JsonObject grandParentCommentObj = new JsonParser().parse(grandParentCommentDoc.toJson()).getAsJsonObject();
                                commentObj.add("grand_parent", grandParentCommentObj);
                            }
                        }
                    }
                }
                comments.add(commentObj);
            }
        } finally {
            cursor.close();
        }

        return comments;
    }
}

