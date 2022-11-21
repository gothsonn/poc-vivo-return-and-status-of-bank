package com.accenture.PocVivoReturnAndStatusOfBank.dao;

import com.accenture.PocVivoReturnAndStatusOfBank.helper.ApiHelper;
import com.accenture.PocVivoReturnAndStatusOfBank.model.FinancialAccount;
import com.accenture.PocVivoReturnAndStatusOfBank.model.FinancialAccountUpdate;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

@Component
public class FinancialAccountDao {

    private static String FINANCIAL_ACCOUNT_COLLECTION_NAME = "financialAccounts";
    private MongoCollection<FinancialAccount> collection;
    private CodecRegistry registry;
    private Integer defaultLimit = 0;

    private final CodecProvider financialAccountProvider = PojoCodecProvider.builder()
            .register(FinancialAccount.class.getPackage().getName())
            .build();

    @Autowired
    public FinancialAccountDao(MongoClient mongoClient, @Value("${db.dbname}") String databaseName, @Value("${db.defaultLimit}") Integer limit) {
        this.registry = fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(financialAccountProvider),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));

        defaultLimit = limit;
        collection = mongoClient.getDatabase(databaseName).
                getCollection(FINANCIAL_ACCOUNT_COLLECTION_NAME, FinancialAccount.class)
                .withCodecRegistry(registry);
    }

    public List<FinancialAccount> getFinancialAccounts(String fields, Integer skip, Integer limit) {
        var accounts = new ArrayList<FinancialAccount>();

        if (skip == null) {
            skip = 0;
        }
        if (limit == null || limit == 0) {
            limit = defaultLimit;
        }
        var projectStage = ApiHelper.createProjectStageFromFieldList(fields);

        var pipeline = new ArrayList<Bson>();

        pipeline.add(skip(skip));
        pipeline.add(limit(limit));

        if (projectStage != null) {
            pipeline.add(projectStage);
        }

        collection.aggregate(pipeline).iterator().forEachRemaining(accounts::add);

        return accounts;
    }

    public FinancialAccount getFinancialAccount(String id, String fields) {
        var projectStage = ApiHelper.createProjectStageFromFieldList(fields);

        var pipeline = new ArrayList<Bson>();

        pipeline.add(match(eq("_id", id)));

        if (projectStage != null) {
            pipeline.add(projectStage);
        }

        var account = collection.aggregate(pipeline).first();

        return account;
    }

    public void deleteFinancialAccount(String id) {
        collection.deleteOne(eq("_id", id));
    }


    public FinancialAccount createFinancialAccount(FinancialAccount account) {
        collection.insertOne(account);
        return account;
    }

    public FinancialAccount updateFinancialAccount(String id, FinancialAccountUpdate financialAccountUpdate) {

        var updates = ApiHelper.convertUpdateObjectToUpdateExpr(financialAccountUpdate);

        if (!updates.isEmpty()) {
            updates.add(set("lastModified", OffsetDateTime.now()));
            updates.add(set("status", "processado"));
            var account = collection.findOneAndUpdate(
                    eq("_id", id),
                    combine(updates),
                    new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER)
            );
            return account;
        }

        return null;
    }
}
