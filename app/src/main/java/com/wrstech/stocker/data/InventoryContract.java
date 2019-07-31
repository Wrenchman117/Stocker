package com.wrstech.stocker.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

    private InventoryContract(){}

    public static final String CONTENT_AUTHORITY = "com.wrstech.stocker";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATHS_STOCKER = "stocker";

    public static final class InventoryEntry implements BaseColumns{
        
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATHS_STOCKER);
        
        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATHS_STOCKER;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATHS_STOCKER;

        public static final String TABLE_NAME = "inventory";

        public static final String _ID = BaseColumns._ID;

        public static final String COLUMN_PRODUCT_NAME = "product_name";

        public static final String COLUMN_PRICE = "price";

        public static final String COLUMN_QUANTITY = "quantity";

        public static final String COLUMN_DESCRIPTION = "description";

        public static final String COLUMN_SUPPLIER = "supplier";

        public static final String COLUMN_PICTURE_URI = "picture";

        public static final int SUPPLIER_UNKNOWN = 0;
        public static final int SUPPLIER_MAKI = 1;
        public static final int SUPPLIER_HENDRIX = 2;
        public static final int SUPPLIER_ABACUS = 3;
        public static final int SUPPLIER_FUTURA = 4;

        public static boolean isValidSupplier(int test) {

            if(test == SUPPLIER_UNKNOWN || test == SUPPLIER_MAKI || test == SUPPLIER_ABACUS || test == SUPPLIER_FUTURA || test == SUPPLIER_HENDRIX){
                return true;
            }

            return false;
        }
    }
}
