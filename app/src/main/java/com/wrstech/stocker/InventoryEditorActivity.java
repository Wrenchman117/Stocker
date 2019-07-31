package com.wrstech.stocker;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.wrstech.stocker.data.InventoryContract;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.wrstech.stocker.data.InventoryContract.InventoryEntry;

public class InventoryEditorActivity extends AppCompatActivity  implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_ITEM_LOADER = 0;

    private Uri mCurrentItemUri;

    private ImageView product_image;
    private ImageView photo_btn;
    private EditText product_name;
    private EditText product_price;
    private Button subBtn;
    private Button addBtn;
    private EditText product_quantity;
    private Spinner supplierSpinner;
    private EditText product_description;

    private int mSupplier = InventoryContract.InventoryEntry.SUPPLIER_UNKNOWN;

    public static final int REQUEST_IMAGE_CAPTURE = 1;

    private boolean mItemHasChanged = false;

    private String currentPhotoPath;

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_layout);

        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if(mCurrentItemUri == null){
            setTitle(getString(R.string.editor_activity_title_new_item));
            invalidateOptionsMenu();
        }else{
            setTitle(getString(R.string.editor_activity_title_edit_pet));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        product_image = (ImageView) findViewById(R.id.detail_photo_image);
        photo_btn = (ImageView) findViewById(R.id.add_photo_image);
        product_name = (EditText) findViewById(R.id.edit_product_name);
        product_price = (EditText) findViewById(R.id.edit_price);
        subBtn = (Button) findViewById(R.id.subtract_btn);
        addBtn = (Button) findViewById(R.id.add_btn);
        product_quantity = (EditText) findViewById(R.id.edit_quantity);
        product_description = (EditText) findViewById(R.id.edit_description);
        supplierSpinner = (Spinner) findViewById(R.id.spinner_supplier);

        product_quantity.setText("0");

        product_name.setOnTouchListener(mOnTouchListener);
        product_price.setOnTouchListener(mOnTouchListener);
        product_quantity.setOnTouchListener(mOnTouchListener);
        product_description.setOnTouchListener(mOnTouchListener);

        setupSpinner();

        product_image.setImageResource(R.mipmap.app_icon);

        photo_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        subBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = product_quantity.getText().toString();
                int num = Integer.parseInt(value) - 1;
                value = String.valueOf(num);
                product_quantity.setText(value);
            }
        });

        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String value = product_quantity.getText().toString();
                int num = Integer.parseInt(value) + 1;
                value = String.valueOf(num);
                product_quantity.setText(value);
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch (IOException ex){

            }

            if(photoFile != null){
                Uri photoUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setPic(currentPhotoPath);
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void setPic(String photoPath) {
        // Get the dimensions of the View
        int targetW = 170;
        int targetH = 170;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);
        product_image.setImageBitmap(bitmap);
        product_image.setRotation(90);
    }

    private void setupSpinner(){

        ArrayAdapter supplierSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_supplier_options, android.R.layout.simple_spinner_item);
        supplierSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        supplierSpinner.setAdapter(supplierSpinnerAdapter);
        supplierSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if(!TextUtils.isEmpty(selection)){
                    if(selection.equals(getString(R.string.supplier_maki))){
                        mSupplier = InventoryContract.InventoryEntry.SUPPLIER_MAKI;
                    }else if(selection.equals(R.string.supplier_hendrix)){
                        mSupplier = InventoryContract.InventoryEntry.SUPPLIER_HENDRIX;
                    }else if(selection.equals(R.string.supplier_abacus)){
                        mSupplier = InventoryContract.InventoryEntry.SUPPLIER_ABACUS;
                    }else if(selection.equals(R.string.supplier_futura)){
                        mSupplier = InventoryContract.InventoryEntry.SUPPLIER_FUTURA;
                    }else{
                        mSupplier = InventoryContract.InventoryEntry.SUPPLIER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mSupplier = InventoryContract.InventoryEntry.SUPPLIER_UNKNOWN;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save pet to database
                saveItem();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(InventoryEditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(InventoryEditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteItem();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle){

        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryContract.InventoryEntry.COLUMN_SUPPLIER,
                InventoryContract.InventoryEntry.COLUMN_PRICE,
                InventoryContract.InventoryEntry.COLUMN_QUANTITY,
                InventoryContract.InventoryEntry.COLUMN_DESCRIPTION,
                InventoryContract.InventoryEntry.COLUMN_PICTURE_URI
        };

        return new CursorLoader(this, mCurrentItemUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor){
        if(cursor == null || cursor.getCount() < 1){
            return;
        }

        if(cursor.moveToFirst()){

            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            int supplierColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_SUPPLIER);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
            int descriptionColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_DESCRIPTION);
            int pictureUriColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PICTURE_URI);

            String name = cursor.getString(nameColumnIndex);
            int supplier = cursor.getInt(supplierColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            String pictureUri = cursor.getString(pictureUriColumnIndex);

            product_name.setText(name);
            product_price.setText(Double.toString(price));
            product_quantity.setText(Integer.toString(quantity));
            product_description.setText(description);

            currentPhotoPath = pictureUri;

            switch (supplier){
                case InventoryEntry.SUPPLIER_MAKI:
                    supplierSpinner.setSelection(1);
                    break;
                case InventoryEntry.SUPPLIER_HENDRIX:
                    supplierSpinner.setSelection(2);
                    break;
                case InventoryEntry.SUPPLIER_ABACUS:
                    supplierSpinner.setSelection(3);
                    break;
                case InventoryEntry.SUPPLIER_FUTURA:
                    supplierSpinner.setSelection(4);
                    break;
                default:
                    supplierSpinner.setSelection(0);
                    break;
            }

            setPic(pictureUri);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader){
        product_name.setText("");
        product_description.setText("");
        product_quantity.setText("");
        product_price.setText("");
        product_image.setImageResource(R.mipmap.app_icon);
    }

    private void saveItem(){

        String nameString = product_name.getText().toString();
        Double priceString = Double.parseDouble(product_price.getText().toString());
        int quantityInt = Integer.parseInt(product_quantity.getText().toString());
        String descriptionString = product_description.getText().toString();
        String pictureUriString = currentPhotoPath;
        int supplierInt = mSupplier;

        if(mCurrentItemUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(descriptionString) && TextUtils.isEmpty(pictureUriString) && priceString < 0 && quantityInt < 0 && supplierInt < 0){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_PRICE, priceString);
        values.put(InventoryEntry.COLUMN_QUANTITY, quantityInt);
        values.put(InventoryEntry.COLUMN_DESCRIPTION, descriptionString);
        values.put(InventoryEntry.COLUMN_PICTURE_URI, pictureUriString);
        values.put(InventoryEntry.COLUMN_SUPPLIER, supplierInt);

        if(mCurrentItemUri == null){
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if(newUri == null){
                Toast.makeText(this, getString(R.string.editor_insert_item_failed), Toast.LENGTH_SHORT);
            }else{
                Toast.makeText(this, getString(R.string.editor_insert_item_succsful), Toast.LENGTH_SHORT);
            }
        }else{
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            if(rowsAffected == 0){
                Toast.makeText(this, getString(R.string.editor_update_item_failed), Toast.LENGTH_SHORT);
            }else{
                Toast.makeText(this, getString(R.string.editor_update_item_succesful), Toast.LENGTH_SHORT);
            }
        }
    }

    private void deleteItem(){

        if(mCurrentItemUri != null){
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            if(rowsDeleted == 0){
                Toast.makeText(this, getString(R.string.editor_delete_item_failed), Toast.LENGTH_SHORT);
            }else{
                Toast.makeText(this, getString(R.string.editor_delete_item_succesful), Toast.LENGTH_SHORT);
            }
        }
    }
}
