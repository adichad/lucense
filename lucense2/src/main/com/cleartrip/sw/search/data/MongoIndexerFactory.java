package com.cleartrip.sw.search.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public class MongoIndexerFactory {

  int                 batch_size = 10000;
  String              mongoHost  = "localhost";
  int                 mongoPort  = 27017;
  String              dbName;
  String              collectionName;
  String              idField;

  private DataManager manager;

  private Integer     commitInterval;
  private DataMapper  dataMapper;

  public MongoIndexerFactory(Map<String, ?> params, Properties env,
      DataManager manager, DataMapper mapper) {
    this.batch_size = (Integer) params.get("batchSize");
    this.mongoHost = (String) params.get("mongoHost");
    this.mongoPort = (Integer) params.get("mongoPort");
    this.dbName = (String) params.get("dbName");
    this.collectionName = (String) params.get("collectionName");
    this.idField = (String) params.get("idField");
    this.commitInterval = (Integer) params.get("commitInterval");
    this.manager = manager;
    this.dataMapper = mapper;
  }

  public MongoIndexer createInstance() {
    return new MongoIndexer(batch_size, mongoHost, mongoPort, dbName,
        collectionName, idField, manager, commitInterval, dataMapper);
  }

  public static class MongoIndexer implements Runnable {
    private HashMap<String, List<String>> aliasesMap       = new HashMap<String, List<String>>();
    int                                   dbHitForAliases  = 0;

    final int                             batch_size;
    final String                          mongoHost;
    final int                             mongoPort;
    final String                          dbName;
    final String                          collectionName;
    final String                          idField;
    final DataManager                     manager;
    final int                             commitInterval;
    private DataMapper                    dataMapper;

    private final SimpleDateFormat        df               = new SimpleDateFormat(
                                                               "dd-MM-yyyy HH:mm:ss");
    public static final String            GZIP             = "gzip";

    public static final String            ACCEPT_ENCODING  = "Accept-Encoding";

    public static final String            CONTENT_ENCODING = "Content-Encoding";

    public static enum Char_Encoding_Type_Enum {
      UTF
    };

    private static final Logger log = LoggerFactory
                                        .getLogger(MongoIndexer.class);

    public MongoIndexer(int batchSize, String mongoHost, int mongoPort,
        String dbName, String collectionName, String idField,
        DataManager manager, int commitInterval, DataMapper dataMapper) {
      this.batch_size = batchSize;
      this.mongoHost = mongoHost;
      this.mongoPort = mongoPort;
      this.dbName = dbName;
      this.collectionName = collectionName;
      this.idField = idField;
      this.commitInterval = commitInterval;
      this.manager = manager;
      this.dataMapper = dataMapper;
    }

    public void run() {
      try {
        long t1 = System.currentTimeMillis();
        Mongo m = new Mongo(mongoHost, mongoPort);
        DB db = m.getDB(dbName);
        DBCollection collection = db.getCollection(collectionName);

        DBCursor c = collection.find().sort(new BasicDBObject(idField, 1))
            .limit(1);
        String minId = c.next().get(idField).toString();

        c = collection.find().sort(new BasicDBObject(idField, -1)).limit(1);
        // String maxId = c.next().get(idField).toString();

        // System.out.println("Min id: " + minId + ", Max id: " + maxId);

        long totalRecords = collection.count();
        int currBatch = 0;
        long total_processed = 0l;
        List<Map<String, ?>> records;
        while (total_processed <= totalRecords) {
          currBatch++;
          BasicDBObject q = new BasicDBObject();

          q.put(idField, new BasicDBObject("$gte", new ObjectId(minId)));

          DBCursor dbc = collection.find(q).sort(new BasicDBObject(idField, 1))
              .limit(batch_size);

          DBCursor dbc1 = collection.find(q)
              .sort(new BasicDBObject(idField, 1)).skip(batch_size).limit(1);

          total_processed = total_processed + dbc.size();

          if (dbc1.hasNext()) {
            minId = dbc1.next().get(idField).toString();

          } else {
            break;
          }
          records = new ArrayList<>(dbc.size());
          while (dbc.hasNext()) {
        	DBObject mongoRecord = dbc.next();        	  
            HashMap<String, Object> record = getRecord(mongoRecord, collection);
            records.add(record);
          }
          manager.upsert(records, currBatch % commitInterval == 0, dataMapper);

        }

        long t2 = System.currentTimeMillis();
        System.out.println("Time taken: " + (t2 - t1) / 1000);
        log.info("background indexer completed successfully for {} in {} secs",
            total_processed, (t2 - t1) / 1000);
      } catch (Exception e) {
        log.info("background indexer aborted. ", e);
        throw new RuntimeException(e);
      }
    }

    public HashMap<String, Object> getRecord(DBObject mongoRecord,
        DBCollection collection) throws Exception {
      HashMap<String, Object> record = new HashMap<String, Object>(30);

      try {

        // setting below items
        // _id, bounding_box, loc, tags, aliases, geo_path_names, continent,
        // geo_path_ids, geo_path_alaises(TODO),

        // set _id
        record.put(idField, mongoRecord.get(idField).toString().toUpperCase());

        // set name as id. Why do we need this? ans: for dedup on exact name string.
        record.put("name_as_id", mongoRecord.get("name"));
        
        // set bounding box
        BasicDBObject d = (BasicDBObject) mongoRecord.get("bounding_box");

        if (d != null) {
          for (String source_name : d.keySet()) {
            BasicDBObject source_specific_bb = (BasicDBObject) d
                .get(source_name);
            BasicDBObject north_east = (BasicDBObject) source_specific_bb
                .get("north_east");
            BasicDBObject south_west = (BasicDBObject) source_specific_bb
                .get("south_west");

            if (north_east.get("latitude") == null) {

              record.put("lat_ne_" + source_name, 91.0);

            } else {

              record.put("lat_ne_" + source_name, ((Double)north_east.get("latitude")).floatValue());

              record.put("str_lat_ne_" + source_name, north_east
                  .get("latitude").toString());
            }

            if (north_east.get("longitude") == null) {

              record.put("long_ne_" + source_name, 181.0);

            } else {
              record.put("long_ne_" + source_name, ((Double)north_east.get("longitude")).floatValue());
              record.put("str_long_ne_" + source_name,
                  north_east.get("longitude").toString());
            }

            if (south_west.get("latitude") == null) {
              record.put("lat_sw_" + source_name, 91.0);
            } else {
              record.put("lat_sw_" + source_name, ((Double) south_west.get("latitude")).floatValue());
              record.put("str_lat_sw_" + source_name, south_west
                  .get("latitude").toString());
            }
            if (south_west.get("longitude") == null) {
              record.put("long_sw_" + source_name, 181.0);
            } else {
              record.put("long_sw_" + source_name, ((Double)south_west.get("longitude")).floatValue());
              record.put("str_long_sw_" + source_name,
                  south_west.get("longitude").toString());
            }
          }
        }

        // set loc i.e. lat longs for each source types
        d = (BasicDBObject) mongoRecord.get("loc");

        if (d != null) {
          for (String source_name : d.keySet()) {
            BasicDBObject source_specific_lat_long = (BasicDBObject) d
                .get(source_name);

            Object lat = source_specific_lat_long.get("latitude");

            if (lat == null) {
              record.put("lat_ce_" + source_name, 91.0);
            } else if (lat instanceof Double) {
              record.put("lat_ce_" + source_name, ((Double)lat).floatValue());
              record.put("str_lat_ce_" + source_name, lat.toString());
            }

            Object longitude = source_specific_lat_long.get("longitude");

            if (longitude == null) {
              record.put("long_ce_" + source_name, 181.0);
            } else if (longitude instanceof Double) {
              record.put("long_ce_" + source_name, ((Double)longitude).floatValue());
              record.put("str_long_ce_" + source_name, longitude.toString());
            }

          }
        }

        // set tags
        d = (BasicDBObject) mongoRecord.get("tags");

        if (d != null) {
          Set<String> tagSet = new HashSet<String>();
          for (String source_name : d.keySet()) {
            Object o = d.get(source_name);
            if (o != null) {
              BasicDBList bdList = (BasicDBList) o;
              Iterator iterator = bdList.iterator();
              while (iterator.hasNext()) {
                String tag = (String) iterator.next();
                if (tag != null) {
                  tagSet.add(tag);
                }
              }
            }
          }
          record.put("tags", tagSet);
        }

        // set aliases
        d = (BasicDBObject) mongoRecord.get("aliases");

        if (d != null) {

          Set<String> aliasesSet = new HashSet<String>();

          for (String source_name : d.keySet()) {

            if (d.get(source_name) != null) {

              BasicDBList bdList = (BasicDBList) d.get(source_name);

              Iterator iterator = bdList.iterator();

              while (iterator.hasNext()) {
                String alias = (String) iterator.next();
                if (alias != null) {
                  aliasesSet.add(alias);
                }

              }
            }
          }
          record.put("aliases", aliasesSet.toArray(new String[aliasesSet.size()]));
        }

        // set geo_path_names
        d = (BasicDBObject) mongoRecord.get("breadcrumbs");

        if (d != null) {

          List<String> geo_path_name_list = new ArrayList<String>(6);

          if (d.get("geo_path_names") != null) {

            BasicDBList bdList = (BasicDBList) d.get("geo_path_names");

            Iterator iterator = bdList.iterator();

            while (iterator.hasNext()) {

              String geo_path_name = (String) iterator.next();
              if (geo_path_name != null) {
                geo_path_name_list.add(geo_path_name.toString());
              }

            }

            record.put("geo_path_names", geo_path_name_list
                .toArray(new String[geo_path_name_list.size()]));

            if (geo_path_name_list.size() > 0) {
              record.put("continent",
                  geo_path_name_list.get(geo_path_name_list.size() - 1));
            } else {
              record.put("continent", null);
            }

          }

          List<String> geo_path_id_list = new ArrayList<String>(6);

          if (d.get("geo_path_ids") != null) {

            BasicDBList bdList = (BasicDBList) d.get("geo_path_ids");

            Iterator iterator = bdList.iterator();

            while (iterator.hasNext()) {

              String geo_path_id = (String) iterator.next();
              if (geo_path_id != null) {
                geo_path_id_list.add(geo_path_id.toString());
              }

            }

            record.put("geo_path_ids",
                geo_path_id_list.toArray(new String[geo_path_id_list.size()]));

          }
          // TODO Need to add geo_path_aliases
        }

        // guide ids
        d = (BasicDBObject) mongoRecord.get("guide_ids");

        if (d != null) {

          String lpGuideId = null;

          if (d.get("lp_guide_id") == null) {
            // update parent lp guide id as lp_guide_id for this record.
            // Similarly,
            // do it for wiki guide ids as well
            if (d.get("parent_lp_guide_id") != null) {
              lpGuideId = d.getString("parent_lp_guide_id");
              record.put("lp_guide_id", lpGuideId);
            }
          } else {
            lpGuideId = d.getString("lp_guide_id");
            record.put("lp_guide_id", lpGuideId);
            // TODO read lp guide text
          }

          String wikiGuideId = null;

          if (d.get("wiki_guide_id") == null) {

            if (d.get("parent_wiki_guide_id") != null) {
              wikiGuideId = d.getString("parent_wiki_guide_id");
              record.put("wiki_guide_id", wikiGuideId);
            }

          } else {
            wikiGuideId = d.getString("wiki_guide_id");
            record.put("wiki_guide_id", wikiGuideId);
            // TODO read lp guide text
          }
        }

        // place_types
        d = (BasicDBObject) mongoRecord.get("place_types");

        if (d != null) {
          String place_type = d.getString("small_world");
          
          if (place_type != null) {
        	  record.put("place_type_small_world", place_type);
          }

          List<String> place_types = new ArrayList<String>();

          for (String source_name : d.keySet()) {

            String place_type_for_source = d.getString(source_name);

            if (place_type_for_source != null) {
            	place_types.add(place_type_for_source);
            }
          }
          record.put("place_type",
              place_types.toArray(new String[place_types.size()]));

        } 

        // source_ids
        d = (BasicDBObject) mongoRecord.get("source_ids");

        if (d != null) {
          record.put("source",
              (String[]) d.keySet().toArray(new String[d.size()]));
          for (String source_name : d.keySet()) {
            String source_id = d.getString(source_name);
            record.put("source_id_" + source_name, source_id);
          }
          record.put("source_count", d.size());
        }

        if (mongoRecord.get("child_count") != null) {
        	String s = mongoRecord.get("child_count").toString();
        	Double doubleVal = Double.valueOf(s);
	        record.put("child_count", doubleVal.intValue());
	        record.put("str_child_count", mongoRecord.get("child_count").toString());
        }

        record
            .put("str_child_count", mongoRecord.get("child_count").toString());

        // record["update_date"] = Time.parse(record["update_date"]).to_i
        // record["create_date"] = Time.parse(record["create_date"]).to_i

        //record.put("update_date",df.parse((String) mongoRecord.get("update_date")).getTime());
        //record.put("create_date",df.parse((String) mongoRecord.get("create_date")).getTime());

        // source_ids
        

        d = (BasicDBObject) mongoRecord.get("hotel_count");

        // TODO rating value is incorrect in DB. Need to fixed by content

        if (d != null) {
          double weighted_hotel_count = 0.0;
          for (String rating : d.keySet()) {
            if ("total".equalsIgnoreCase(rating)) {
              record.put("hotel_count", d.get(rating));
            } else {
              double ratingValue = d.getDouble(rating);
              rating = rating.replace(":", ".");
              weighted_hotel_count = weighted_hotel_count
                  + (Double.valueOf(rating) * ratingValue);
            }
          }
          
          record.put("weighted_hotel_count", (float) weighted_hotel_count);
        }

        // star rating

        if (mongoRecord.get("star_rating") != null) {
          record.put("star_rating", mongoRecord.get("star_rating"));
          record.put("str_star_rating", mongoRecord.get("star_rating")
              .toString());
        }

        // ta_rating
        if (mongoRecord.get("ta_rating") != null) {
          record.put("ta_rating", mongoRecord.get("ta_rating"));
          record.put("str_ta_rating", mongoRecord.get("ta_rating").toString());
        }

        // hotel_avg_price
        if (mongoRecord.get("hotel_avg_price") != null) {
          Double doubleValue = Double.valueOf(mongoRecord.get("hotel_avg_price").toString());
          record.put("hotel_avg_price", doubleValue.floatValue());
          record.put("str_hotel_avg_price", mongoRecord.get("hotel_avg_price").toString());
        }

        // hotel_low_price

        if (mongoRecord.get("hotel_low_price") != null) {
        	Double doubleValue = Double.valueOf(mongoRecord.get("hotel_low_price").toString());
        	record.put("hotel_low_price", doubleValue.floatValue());
        }

        // hotel_high_price
        if (mongoRecord.get("hotel_high_price") != null) {
        	Double doubleValue = Double.valueOf(mongoRecord.get("hotel_high_price").toString());
        	record.put("hotel_high_price", doubleValue.floatValue());
        }

        // hotel_high_price
        if (mongoRecord.get("ta_total_ratings") != null) {
          record.put("ta_total_ratings", mongoRecord.get("ta_total_ratings"));
          record.put("str_ta_total_ratings", mongoRecord.get("ta_total_ratings").toString());
        }

        if (record.get("geo_path_ids") != null && record.get("geo_path_names") != null) {

          int i = 0;

          List<String> geo_aliases = new ArrayList<String>();

          String[] geo_path_names = (String[]) record.get("geo_path_names");

          for (String geo_id : (String[]) record.get("geo_path_ids")) {

            geo_aliases.add(geo_path_names[i]);

            if (aliasesMap.containsKey(geo_id)) {

              geo_aliases.addAll(aliasesMap.get(geo_id));

            } else {

              dbHitForAliases++;

              List<String> geo_id_aliases = new ArrayList<String>();

              BasicDBObject query = new BasicDBObject(idField, new ObjectId(
                  geo_id));

              DBObject dbObject = collection.findOne(query);

              if (dbObject != null) {

                d = (BasicDBObject) dbObject.get("aliases");

                if (d != null) {

                  Collection<Object> aliasesList = d.values();

                  for (Object obj : aliasesList) {
                    BasicDBList bd = (BasicDBList) obj;
                    if (bd != null) {
                      for (Object o : bd) {
                    	  if (o != null) {
                    		  geo_id_aliases.add((String) o);
                    	  }
                      }
                    }

                  }

                }
              }
              aliasesMap.put(geo_id, geo_id_aliases);

              geo_aliases.addAll(geo_id_aliases);

            }

            i++;
          }
          record.put("geo_path_aliases",
              geo_aliases.toArray(new String[geo_aliases.size()]));

        }
        
        
        //other fields need to be populated as it is.
        if (mongoRecord.get("is_active") != null) {
        	record.put("is_active", mongoRecord.get("is_active").toString());
        }
        
        if (mongoRecord.get("name") != null) {
        	record.put("name", mongoRecord.get("name").toString());
        }
        
        if (mongoRecord.get("language") != null) {
        	record.put("language", mongoRecord.get("language").toString());
        }
        
        if (mongoRecord.get("country_code") != null) {
        	record.put("country_code", mongoRecord.get("country_code").toString());
        }

        if (mongoRecord.get("bookings_domestic") != null) {
        	record.put("bookings_domestic", mongoRecord.get("bookings_domestic"));
        }

        if (mongoRecord.get("bookings_international") != null) {
        	record.put("bookings_international", mongoRecord.get("bookings_international"));
        }
        
        if (mongoRecord.get("continent") != null) {
        	record.put("continent", mongoRecord.get("continent").toString());
        }
        
        if (mongoRecord.get("hotel_img_url") != null) {
        	record.put("hotel_img_url", mongoRecord.get("hotel_img_url").toString());
        }
        
      } catch (Exception e) {
        System.out.println(mongoRecord);
        throw e;
      }
      // System.out.println(record);
      return record;
    }

    /**
     * Sends a GET request to the given url and returns the response as a String
     * 
     * @return String
     * @throws MalformedURLException
     * @throws IOException
     */
    public static String get(String urlLocation, String queryString,
        Map<String, String> requestHeaders,
        Map<String, List<String>> responseHeaders,
        Char_Encoding_Type_Enum encoding_type) throws MalformedURLException,
        IOException {

      URLConnection connection = null;
      BufferedReader reader = null;
      StringBuffer sb = new StringBuffer();
      if (queryString != null && !queryString.isEmpty()) {
        urlLocation = urlLocation + "?" + queryString;
      }

      // log.info("making get call to : \n" + urlLocation);

      try {
        URL server = new URL(urlLocation);
        connection = server.openConnection();
        connection.setConnectTimeout(120000);
        connection.setReadTimeout(120000);

        if (requestHeaders != null) {
          for (String key : requestHeaders.keySet()) {
            connection.setRequestProperty(key, requestHeaders.get(key));
          }
        }
        // addCallerInfoHeader(server, connection);
        connection.setRequestProperty(ACCEPT_ENCODING, GZIP);

        InputStream responseStream = connection.getInputStream();
        if (GZIP.equalsIgnoreCase(connection.getHeaderField(CONTENT_ENCODING))) {
          responseStream = new GZIPInputStream(responseStream);
        }
        if (encoding_type == Char_Encoding_Type_Enum.UTF) {
          reader = new BufferedReader(new InputStreamReader(responseStream,
              "UTF-8"));
        } else {
          reader = new BufferedReader(new InputStreamReader(responseStream));
        }

        String line = null;

        while ((line = reader.readLine()) != null) {
          sb.append(line).append('\n');
        }

      } catch (MalformedURLException malEx) {
        System.out.println(malEx);
        // log.error(malEx);
      } catch (FileNotFoundException fnfEx) {
        // log.error("FileNotFoundException exception for URL-"+urlLocation,
        // fnfEx);
        System.out.println("FileNotFoundException exception for URL-"
            + urlLocation);

      } catch (IOException ioEx) {
      } finally {
        if (reader != null) {
          try {
            reader.close();
          } catch (Exception e) {
          }
        }
      }

      return sb.toString();
    }
  }
}
