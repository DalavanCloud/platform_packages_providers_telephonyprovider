/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.android.providers.telephony;

import static android.provider.Telephony.Carriers;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.test.InstrumentationRegistry;
import android.telephony.SubscriptionManager;
import android.test.suitebuilder.annotation.SmallTest;
import android.text.TextUtils;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * To run this test, run the following from the dir: packages/providers/TelephonyProvider
 *    atest TelephonyProviderTests:TelephonyDatabaseHelperTest
 * Or
 *    runtest --path tests/src/com/android/providers/telephony/TelephonyDatabaseHelperTest.java
 */
@RunWith(JUnit4.class)
public final class TelephonyDatabaseHelperTest {

    private final static String TAG = TelephonyDatabaseHelperTest.class.getSimpleName();

    private Context mContext;
    private TelephonyProvider.DatabaseHelper mHelper; // the actual class being tested
    private SQLiteOpenHelper mInMemoryDbHelper; // used to give us an in-memory db

    @Before
    public void setUp() {
        Log.d(TAG, "setUp() +");
        mContext = InstrumentationRegistry.getContext();
        mHelper = new TelephonyProvider.DatabaseHelper(mContext);
        mInMemoryDbHelper = new InMemoryTelephonyProviderV5DbHelper();
        Log.d(TAG, "setUp() -");
    }

    @Test
    public void databaseHelperOnUpgrade_hasApnSetIdField() {
        Log.d(TAG, "databaseHelperOnUpgrade_hasApnSetIdField");
        // (5 << 16 | 6) is the first upgrade trigger in onUpgrade
        SQLiteDatabase db = mInMemoryDbHelper.getWritableDatabase();
        mHelper.onUpgrade(db, (4 << 16), TelephonyProvider.DatabaseHelper.getVersion(mContext));

        // the upgraded db must have the APN_SET_ID field
        Cursor cursor = db.query("carriers", null, null, null, null, null, null);
        String[] upgradedColumns = cursor.getColumnNames();
        Log.d(TAG, "carriers columns: " + Arrays.toString(upgradedColumns));

        assertTrue(Arrays.asList(upgradedColumns).contains(Carriers.APN_SET_ID));
    }

    @Test
    public void databaseHelperOnUpgrade_hasCarrierIdField() {
        Log.d(TAG, "databaseHelperOnUpgrade_hasSubscriptionTypeField");
        // (5 << 16 | 6) is the first upgrade trigger in onUpgrade
        SQLiteDatabase db = mInMemoryDbHelper.getWritableDatabase();
        mHelper.onUpgrade(db, (4 << 16), TelephonyProvider.DatabaseHelper.getVersion(mContext));

        // the upgraded db must have the Telephony.Carriers.CARRIER_ID field
        Cursor cursor = db.query("carriers", null, null, null, null, null, null);
        String[] upgradedColumns = cursor.getColumnNames();
        Log.d(TAG, "carriers columns: " + Arrays.toString(upgradedColumns));
        assertTrue(Arrays.asList(upgradedColumns).contains(Carriers.CARRIER_ID));
    }

    @Test
    public void databaseHelperOnUpgrade_hasCountryIsoField() {
        Log.d(TAG, "databaseHelperOnUpgrade_hasCountryIsoField");
        SQLiteDatabase db = mInMemoryDbHelper.getWritableDatabase();
        mHelper.onUpgrade(db, (4 << 16), TelephonyProvider.DatabaseHelper.getVersion(mContext));

        // the upgraded db must have the Telephony.Carriers.CARRIER_ID field
        Cursor cursor = db.query("simInfo", null, null, null, null, null, null);
        String[] upgradedColumns = cursor.getColumnNames();
        Log.d(TAG, "iso columns: " + Arrays.toString(upgradedColumns));
        assertTrue(Arrays.asList(upgradedColumns).contains(SubscriptionManager.ISO_COUNTRY_CODE));
    }

    @Test
    public void databaseHelperOnUpgrade_hasProfileClassField() {
        Log.d(TAG, "databaseHelperOnUpgrade_hasProfileClassField");
        SQLiteDatabase db = mInMemoryDbHelper.getWritableDatabase();
        mHelper.onUpgrade(db, (4 << 16), TelephonyProvider.DatabaseHelper.getVersion(mContext));

        // the upgraded db must have the PROFILE_CLASS field
        Cursor cursor = db.query("siminfo", null, null, null, null, null, null);
        String[] upgradedColumns = cursor.getColumnNames();
        Log.d(TAG, "profile class columns: " + Arrays.toString(upgradedColumns));
        assertTrue(Arrays.asList(upgradedColumns).contains(SubscriptionManager.PROFILE_CLASS));
    }

    @Test
    public void databaseHelperOnUpgrade_columnsMatchNewlyCreatedDb() {
        Log.d(TAG, "databaseHelperOnUpgrade_columnsMatchNewlyCreatedDb");
        // (5 << 16 | 6) is the first upgrade trigger in onUpgrade
        SQLiteDatabase db = mInMemoryDbHelper.getWritableDatabase();
        mHelper.onUpgrade(db, (4 << 16), TelephonyProvider.DatabaseHelper.getVersion(mContext));

        // compare upgraded carriers table to a carriers table created from scratch
        db.execSQL(TelephonyProvider.getStringForCarrierTableCreation("carriers_full"));

        Cursor cursor = db.query("carriers", null, null, null, null, null, null);
        String[] upgradedColumns = cursor.getColumnNames();
        Log.d(TAG, "carriers columns: " + Arrays.toString(upgradedColumns));

        cursor = db.query("carriers_full", null, null, null, null, null, null);
        String[] fullColumns = cursor.getColumnNames();
        Log.d(TAG, "carriers_full colunmns: " + Arrays.toString(fullColumns));

        assertArrayEquals("Carriers table from onUpgrade doesn't match full table",
                fullColumns, upgradedColumns);

        // compare upgraded siminfo table to siminfo table created from scratch
        db.execSQL(TelephonyProvider.getStringForSimInfoTableCreation("siminfo_full"));

        cursor = db.query("siminfo", null, null, null, null, null, null);
        upgradedColumns = cursor.getColumnNames();
        Log.d(TAG, "siminfo columns: " + Arrays.toString(upgradedColumns));

        cursor = db.query("siminfo_full", null, null, null, null, null, null);
        fullColumns = cursor.getColumnNames();
        Log.d(TAG, "siminfo_full colunmns: " + Arrays.toString(fullColumns));

        assertArrayEquals("Siminfo table from onUpgrade doesn't match full table",
                fullColumns, upgradedColumns);
    }

    @Test
    public void databaseHelperOnUpgrade_hasSubscriptionTypeField() {
        Log.d(TAG, "databaseHelperOnUpgrade_hasSubscriptionTypeField");
        // (5 << 16 | 6) is the first upgrade trigger in onUpgrade
        SQLiteDatabase db = mInMemoryDbHelper.getWritableDatabase();
        mHelper.onUpgrade(db, (4 << 16), TelephonyProvider.DatabaseHelper.getVersion(mContext));

        // the upgraded db must have the SubscriptionManager.SUBSCRIPTION_TYPE field
        Cursor cursor = db.query("siminfo", null, null, null, null, null, null);
        String[] upgradedColumns = cursor.getColumnNames();
        Log.d(TAG, "siminfo columns: " + Arrays.toString(upgradedColumns));

        assertTrue(Arrays.asList(upgradedColumns).contains(SubscriptionManager.SUBSCRIPTION_TYPE));
    }

    /**
     * Helper for an in memory DB used to test the TelephonyProvider#DatabaseHelper.
     *
     * We pass this in-memory db to DatabaseHelper#onUpgrade so we can use the actual function
     * without using the actual telephony db.
     */
    private static class InMemoryTelephonyProviderV5DbHelper extends SQLiteOpenHelper {

        public InMemoryTelephonyProviderV5DbHelper() {
            super(InstrumentationRegistry.getContext(),
                    null,    // db file name is null for in-memory db
                    null,    // CursorFactory is null by default
                    1);      // in-memory db version doesn't seem to matter
            Log.d(TAG, "InMemoryTelephonyProviderV5DbHelper creating in-memory database");
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // Set up the carriers table without any fields added in onUpgrade
            // since these are the initial fields, there is no need to update this test fixture in
            // the future
            List<String> originalUniqueFields = new ArrayList<String>();
            originalUniqueFields.add(Carriers.NUMERIC);
            originalUniqueFields.add(Carriers.MCC);
            originalUniqueFields.add(Carriers.MNC);
            originalUniqueFields.add(Carriers.APN);
            originalUniqueFields.add(Carriers.PROXY);
            originalUniqueFields.add(Carriers.PORT);
            originalUniqueFields.add(Carriers.MMSPROXY);
            originalUniqueFields.add(Carriers.MMSPORT);
            originalUniqueFields.add(Carriers.MMSC);
            Log.d(TAG, "InMemoryTelephonyProviderV5DbHelper onCreate creating the carriers table");
            db.execSQL(
                    "CREATE TABLE carriers" +
                    "(_id INTEGER PRIMARY KEY," +
                    Carriers.NAME + " TEXT DEFAULT ''," +
                    Carriers.NUMERIC + " TEXT DEFAULT ''," +
                    Carriers.MCC + " TEXT DEFAULT ''," +
                    Carriers.MNC + " TEXT DEFAULT ''," +
                    Carriers.APN + " TEXT DEFAULT ''," +
                    Carriers.USER + " TEXT DEFAULT ''," +
                    Carriers.SERVER + " TEXT DEFAULT ''," +
                    Carriers.PASSWORD + " TEXT DEFAULT ''," +
                    Carriers.PROXY + " TEXT DEFAULT ''," +
                    Carriers.PORT + " TEXT DEFAULT ''," +
                    Carriers.MMSPROXY + " TEXT DEFAULT ''," +
                    Carriers.MMSPORT + " TEXT DEFAULT ''," +
                    Carriers.MMSC + " TEXT DEFAULT ''," +
                    Carriers.TYPE + " TEXT DEFAULT ''," +
                    Carriers.CURRENT + " INTEGER," +
                    "UNIQUE (" + TextUtils.join(", ", originalUniqueFields) + "));");

            // set up the siminfo table without any fields added in onUpgrade
            // since these are the initial fields, there is no need to update this test fixture in
            // the future
            Log.d(TAG, "InMemoryTelephonyProviderV5DbHelper onCreate creating the siminfo table");
            db.execSQL(
                    "CREATE TABLE siminfo ("
                    + SubscriptionManager.UNIQUE_KEY_SUBSCRIPTION_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + SubscriptionManager.ICC_ID + " TEXT NOT NULL,"
                    + SubscriptionManager.SIM_SLOT_INDEX
                        + " INTEGER DEFAULT " + SubscriptionManager.SIM_NOT_INSERTED + ","
                    + SubscriptionManager.DISPLAY_NAME + " TEXT,"
                    + SubscriptionManager.NAME_SOURCE
                        + " INTEGER DEFAULT " + SubscriptionManager.NAME_SOURCE_DEFAULT_SOURCE + ","
                    + SubscriptionManager.COLOR
                        + " INTEGER DEFAULT " + SubscriptionManager.COLOR_DEFAULT + ","
                    + SubscriptionManager.NUMBER + " TEXT,"
                    + SubscriptionManager.DISPLAY_NUMBER_FORMAT + " INTEGER NOT NULL"
                        + " DEFAULT " + SubscriptionManager.DISPLAY_NUMBER_DEFAULT + ","
                    + SubscriptionManager.DATA_ROAMING
                        + " INTEGER DEFAULT " + SubscriptionManager.DATA_ROAMING_DEFAULT + ","
                    + SubscriptionManager.CARD_ID + " TEXT NOT NULL"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.d(TAG, "InMemoryTelephonyProviderV5DbHelper onUpgrade doing nothing");
            return;
        }
    }
}
