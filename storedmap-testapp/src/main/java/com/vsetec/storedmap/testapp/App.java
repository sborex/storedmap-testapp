/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.vsetec.storedmap.testapp;

import com.vsetec.storedmap.Category;
import com.vsetec.storedmap.MixedDriver;
import com.vsetec.storedmap.Store;
import com.vsetec.storedmap.StoredMap;
import com.vsetec.storedmap.elasticsearch.ElasticsearchDriver;
import com.vsetec.storedmap.jdbc.GenericJdbcDriver;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author Fyodor Kravchenko <fedd@vsetec.com>
 */
public class App {

    public static void main(String[] args) {

        Properties elasticsearch = new Properties();
        elasticsearch.setProperty("storedmap.applicationCode", "testapp");
        elasticsearch.setProperty("storedmap.driver", ElasticsearchDriver.class.getName());
        elasticsearch.setProperty("storedmap.elasticsearch.host", "localhost");
        elasticsearch.setProperty("storedmap.elasticsearch.port", "9200");

        Properties postgres = new Properties();
        postgres.setProperty("storedmap.applicationCode", "testapp");
        postgres.setProperty("storedmap.driver", GenericJdbcDriver.class.getName());
        postgres.setProperty("storedmap.jdbc.driver", "org.postgresql.Driver");
        postgres.setProperty("storedmap.jdbc.url", "jdbc:postgresql://localhost:5432/testapp04");
        postgres.setProperty("storedmap.jdbc.user", "postgres");
        postgres.setProperty("storedmap.jdbc.password", "postgres");
        postgres.setProperty("storedmap.jdbc.queries.create",
                "create table @{indexName}_main (id varchar(200) primary key, val bytea);\n"
                + "create table @{indexName}_lock (id varchar(200) primary key, createdat timestamp, waitfor integer);\n"
                + "create table @{indexName}_indx (id varchar(200), tag varchar(200), sort varchar(200), map text, primary key (tag, id));\n"
                + "create index @{indexName}_ind1 on @{indexName}_indx (sort, tag);\n"
                + "create index @{indexName}_ind2 on @{indexName}_indx (id)");

        Properties derby = new Properties();
        derby.setProperty("storedmap.applicationCode", "testapp");
        derby.setProperty("storedmap.driver", GenericJdbcDriver.class.getName());
        derby.setProperty("storedmap.jdbc.driver", "org.apache.derby.jdbc.EmbeddedDriver");
        derby.setProperty("storedmap.jdbc.url", "jdbc:derby:testapp;create=true");

        Properties mixed = new Properties();
        mixed.putAll(elasticsearch);
        mixed.putAll(postgres);
        mixed.setProperty("storedmap.driver", MixedDriver.class.getName());
        mixed.setProperty("storedmap.driver.main", GenericJdbcDriver.class.getName());
        mixed.setProperty("storedmap.driver.additional", ElasticsearchDriver.class.getName());

        Store store = Store.getStore(elasticsearch);

        Category category = store.get("Русский");

        for (int i = 0; i < 2; i++) {
            StoredMap map = category.map("map" + i);

            for (int j = 0; j < 4; j++) {
                map.put("key" + j, "value" + j + " of map " + i);
            }
        }

        for (int i = 2; i < 4; i++) {
            HashMap<String, Object> map = new HashMap<>();
            for (int j = 0; j < 4; j++) {
                map.put("key" + j, "value" + j + " of map " + i + " that was put in a different way");
            }
            // testing map interface
            category.put("map" + i, map);

        }

        System.out.println("\nMaps in category " + category.name() + ":");
        for (StoredMap map : category.maps()) {
            System.out.println("\nMap id:\t" + map.key());
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                System.out.println("Key:\t" + entry.getKey() + "\tvalue:\t" + entry.getValue());
            }
        }

        System.out.println("\nCategories:");
        
        for(Category cat: store.categories()){
                System.out.println("Name:\t" + cat.name());
        }

        store.close();
    }
}
