package com.wrstech.stocker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wrstech.stocker.data.InventoryContract;

public class ItemCursorAdapter extends CursorAdapter {

    public ItemCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        ImageView product_image = (ImageView) view.findViewById(R.id.item_image);
        TextView product_name = (TextView) view.findViewById(R.id.product_name_text);
        TextView price_text = (TextView) view.findViewById(R.id.price_text);
        TextView stock_num_text = (TextView) view.findViewById(R.id.in_stock_text);
        Button saleBtn = (Button) view.findViewById(R.id.sales_btn);

        product_image.setImageResource(R.mipmap.app_icon);

        int imageColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PICTURE_URI);
        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE);
        int stockNumColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
        final Uri itemUri = cursor.getNotificationUri();


        String productImage = cursor.getString(imageColumnIndex);
        String itemName = cursor.getString(nameColumnIndex);
        Double price = cursor.getDouble(priceColumnIndex);
        final int stockNumber = cursor.getInt(stockNumColumnIndex);

        int targetW = 80;//product_image.getWidth();
        int targetH = 100;//product_image.getHeight();

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(productImage, bmOptions);

        product_image.setImageBitmap(bitmap);
        product_image.setRotation(90);
        product_name.setText(itemName);
        price_text.setText(String.valueOf(price));
        stock_num_text.setText(String.valueOf(stockNumber));

        saleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int current_quantity = stockNumber - 1;
                Uri currentUri = itemUri;
                saveItem(current_quantity, currentUri, context);
            }
        });
    }

    private void saveItem(int count, Uri mCurrentItemUri, Context context){

        int quantityInt = count;

        if(mCurrentItemUri == null && count < 0){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, count);

        int rowsAffected = context.getContentResolver().update(mCurrentItemUri, values, null, null);

    }
}
