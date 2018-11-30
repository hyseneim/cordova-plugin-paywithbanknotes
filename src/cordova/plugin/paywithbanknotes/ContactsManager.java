package cordova.plugin.paywithbanknotes;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsManager {

    private static final String TAG = "ContactsManager";

    private static final String MIMETYPE =
            "vnd.android.cursor.item/it.altran.ionic.banknotes.premium";

    public static void addContact(Context context, MyContact contact) {
        ContentResolver resolver = context.getContentResolver();
        /*
		resolver.delete(
                RawContacts.CONTENT_URI, RawContacts.ACCOUNT_TYPE + " = ?",
                new String[]{AccountGeneral.ACCOUNT_TYPE});
		*/

        ArrayList<ContentProviderOperation> ops =
                new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newInsert(
                addCallerIsSyncAdapterParameter(RawContacts.CONTENT_URI, true))
                .withValue(RawContacts.ACCOUNT_NAME, AccountGeneral.ACCOUNT_NAME)
                .withValue(RawContacts.ACCOUNT_TYPE, AccountGeneral.ACCOUNT_TYPE)
                .build());

        ops.add(ContentProviderOperation.newInsert(
                addCallerIsSyncAdapterParameter(Settings.CONTENT_URI, true))
                .withValue(RawContacts.ACCOUNT_NAME, AccountGeneral.ACCOUNT_NAME)
                .withValue(RawContacts.ACCOUNT_TYPE, AccountGeneral.ACCOUNT_TYPE)
                .withValue(Settings.UNGROUPED_VISIBLE, 1)
                .build());

        ops.add(ContentProviderOperation.newInsert(
                addCallerIsSyncAdapterParameter(Data.CONTENT_URI, true))
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.GIVEN_NAME, contact.name)
                .withValue(StructuredName.FAMILY_NAME, contact.lastName)
                .build());

        ops.add(ContentProviderOperation.newInsert(
                addCallerIsSyncAdapterParameter(Data.CONTENT_URI, true))
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, MIMETYPE)
                .withValue(Data.DATA4, "Paga con Bank-Notes")
                .withValue(Data.DATA5, "Paga con Bank-Notes")
                .withValue(Data.DATA6, "Paga con Bank-Notes")
                .withValue(Data.DATA7, "test")
                .build());

        ops.add(ContentProviderOperation.newInsert(
                addCallerIsSyncAdapterParameter(Data.CONTENT_URI, true))
                .withValueBackReference(Data.RAW_CONTACT_ID, 0)
                .withValue(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE)
                .withValue(Note.NOTE, contact.iban)
                .build());

        try {
            ContentProviderResult[] results =
                    resolver.applyBatch(ContactsContract.AUTHORITY, ops);
            if (results.length == 0) {
                ;
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, e.getMessage(), e);
        }
    }

    private static Uri addCallerIsSyncAdapterParameter(
            Uri uri, boolean isSyncOperation) {
        if (isSyncOperation) {
            return uri.buildUpon()
                    .appendQueryParameter(
                            ContactsContract.CALLER_IS_SYNCADAPTER, "true")
                    .build();
        }
        return uri;
    }

    public static MyContact findContactByDisplayName(Context context, String displayName, boolean findByAccountType) {
        MyContact result = null;

        StringBuilder filter = new StringBuilder(StructuredName.DISPLAY_NAME + "= ?");
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(displayName);

        if (findByAccountType) {
            filter.append(" AND ");
            filter.append(RawContacts.ACCOUNT_TYPE);
            filter.append(" = ?");
            selectionArgs.add(AccountGeneral.ACCOUNT_TYPE);
        }

        Cursor cursor =
                context.getContentResolver().query(Data.CONTENT_URI, new String[]{
                                Data.RAW_CONTACT_ID, Data.DISPLAY_NAME, Data.MIMETYPE,
                                Data.CONTACT_ID,
                                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME
                        },
                        filter.toString(),
                        selectionArgs.toArray(new String[0]),
                        null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Log.i(TAG, "Raw contact ID: " + cursor.getString(0));
                Log.i(TAG, "Display name: " + cursor.getString(1));
                Log.i(TAG, "Mimetype: " + cursor.getString(2));
                Log.i(TAG, "Contact ID: " + cursor.getString(3));
                String givenName = cursor.getString(4);
                String familyName = cursor.getString(5);
                Log.i(TAG, "GivenName: " + givenName);
                Log.i(TAG, "FamilyName: " + familyName);
                result = new MyContact(cursor.getInt(0), givenName, familyName);
            } while (cursor.moveToNext());
        }

        return result;
    }

    public static MyContact findContactById(Context context, String id, boolean findByAccountType) {
        MyContact result = null;

        StringBuilder filter = new StringBuilder(Data.RAW_CONTACT_ID + "= ?");
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(id);

        if (findByAccountType) {
            filter.append(" AND ");
            filter.append(RawContacts.ACCOUNT_TYPE);
            filter.append(" = ?");
            selectionArgs.add(AccountGeneral.ACCOUNT_TYPE);
        }

        Cursor cursor =
                context.getContentResolver().query(Data.CONTENT_URI, new String[]{
                                Data.RAW_CONTACT_ID, Data.DISPLAY_NAME, Data.MIMETYPE,
                                Data.CONTACT_ID,
                                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME
                        },
                        filter.toString(),
                        selectionArgs.toArray(new String[0]),
                        null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Log.i(TAG, "Raw contact ID: " + cursor.getString(0));
                Log.i(TAG, "Display name: " + cursor.getString(1));
                Log.i(TAG, "Mimetype: " + cursor.getString(2));
                Log.i(TAG, "Contact ID: " + cursor.getString(3));
                String givenName = cursor.getString(4);
                String familyName = cursor.getString(5);
                Log.i(TAG, "GivenName: " + givenName);
                Log.i(TAG, "FamilyName: " + familyName);
                for (int i = 4; i < cursor.getColumnCount(); i++) {
                    Log.i(TAG, "Column: " + cursor.getString(i));
                }
                result = new MyContact(cursor.getInt(0), givenName, familyName);
            } while (cursor.moveToNext());
        }

        return result;
    }

    public static void updateContactByDisplayName(Context context, String displayName) {
        Log.i(TAG, "updateContact with displayName: " + displayName);

        MyContact contact = findContactByDisplayName(context, displayName, false);

        if (contact != null) {
            Log.i(TAG, "Contact found");

            addContact(context, contact);

            MyContact addedContactId = findContactByDisplayName(context, displayName, true);

            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            ops.add(ContentProviderOperation.newUpdate(ContactsContract.AggregationExceptions.CONTENT_URI)
                    .withValue(ContactsContract.AggregationExceptions.TYPE, ContactsContract.AggregationExceptions.TYPE_KEEP_TOGETHER)
                    .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID1, addedContactId.id)
                    .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID2, contact.id).build());

            try {
                ContentProviderResult[] results = context.getContentResolver().applyBatch(
                        ContactsContract.AUTHORITY, ops);
                for (ContentProviderResult result : results) {
                    Log.i(TAG, result.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            Log.i(TAG, "Contact not found");
        }
    }

    public static void updateContactById(Context context, String id, String iban) {
        Log.i(TAG, "updateContact with id: " + id);

        MyContact contact = findContactById(context, id, false);
        contact.iban = iban;

        if (contact != null) {
            Log.i(TAG, "Contact found");

            addContact(context, contact);

            MyContact addedContactId = findContactById(context, id, true);

            ArrayList<ContentProviderOperation> ops = new ArrayList<>();

            ops.add(ContentProviderOperation.newUpdate(ContactsContract.AggregationExceptions.CONTENT_URI)
                    .withValue(ContactsContract.AggregationExceptions.TYPE, ContactsContract.AggregationExceptions.TYPE_KEEP_TOGETHER)
                    .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID1, addedContactId.id)
                    .withValue(ContactsContract.AggregationExceptions.RAW_CONTACT_ID2, contact.id).build());

            try {
                ContentProviderResult[] results = context.getContentResolver().applyBatch(
                        ContactsContract.AUTHORITY, ops);
                for (ContentProviderResult result : results) {
                    Log.i(TAG, result.toString());
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        } else {
            Log.i(TAG, "Contact not found");
        }
    }

}
