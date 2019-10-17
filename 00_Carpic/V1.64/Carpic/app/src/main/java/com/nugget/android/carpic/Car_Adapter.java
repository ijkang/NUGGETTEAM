package com.nugget.android.carpic;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import java.util.List;


public class Car_Adapter extends ArrayAdapter<Car_Info> {

    Context mCtx;
    int listLayoutRes;
    List<Car_Info> carList;
    SQLiteDatabase mDatabase;

    public Car_Adapter(Context mCtx, int listLayoutRes, List<Car_Info> carList, SQLiteDatabase mDatabase) {
        super(mCtx, listLayoutRes, carList);

        this.mCtx = mCtx;
        this.listLayoutRes = listLayoutRes;
        this.carList = carList;
        this.mDatabase = mDatabase;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(listLayoutRes, null);

        final Car_Info car = carList.get(position);


        TextView textViewcardate = view.findViewById(R.id.textViewcardate);
        TextView textViewcarnum = view.findViewById(R.id.textViewcarnum);
        TextView textViewowner = view.findViewById(R.id.textViewcarowner);
        TextView textViewmemo = view.findViewById(R.id.textViewcarmemo);


        textViewcardate.setText(car.getMcardate());
        textViewcarnum.setText(String.valueOf(car.getMcarnum()));
        textViewowner.setText(car.getMowner());
        textViewmemo.setText(car.getMmemo());


        ImageView buttonDelete = view.findViewById(R.id.buttonDeleteCar);
        ImageView buttonEdit = view.findViewById(R.id.buttonEditCar);

        //adding a clicklistener to button
        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateCar(car);
            }
        });

        //the delete operation
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
                builder.setTitle("삭제하시겠습니까?");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String sql = "DELETE FROM CARLIST WHERE id = ?";
                        mDatabase.execSQL(sql, new Integer[]{car.getId()});
                        reloadCarsFromDatabase();
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return view;
    }

    private void updateCar(final Car_Info car) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);

        LayoutInflater inflater = LayoutInflater.from(mCtx);
        View view = inflater.inflate(R.layout.car_update, null);
        builder.setView(view);


        final EditText editTextcardate = view.findViewById(R.id.editTextcardate);
        final EditText editcarnum = view.findViewById(R.id.editcarnum);
        final EditText editowner = view.findViewById(R.id.editowner);
        final EditText editmemo = view.findViewById(R.id.editmemo);

        editTextcardate.setText(car.getMcardate());
        editcarnum.setText(car.getMcarnum());
        editowner.setText(car.getMowner());
        editmemo.setText(car.getMmemo());


        final AlertDialog dialog = builder.create();
        dialog.show();

        // CREATE METHOD FOR EDIT THE FORM
        view.findViewById(R.id.buttonUpdateCar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cardate = editTextcardate.getText().toString().trim();
                String carnum = editcarnum.getText().toString().trim();
                String owner = editowner.getText().toString().trim();
                String carmemo = editmemo.getText().toString().trim();

                if (cardate.isEmpty()) {
                    editTextcardate.setError("날짜를 입력해주세요");
                    editTextcardate.requestFocus();
                    return;
                }

                if (owner.isEmpty()) {
                    editowner.setError("소유주를 입력해주세요");
                    editowner.requestFocus();
                    return;
                }

                String sql = "UPDATE CARLIST \n" +
                        "SET cardate = ?, \n" +
                        "carnum = ?,\n"+
                        "owner = ?,\n"+
                        "carmemo= ? \n" +
                        "WHERE id = ?;\n";

                mDatabase.execSQL(sql, new String[]{cardate, carnum, owner, carmemo, String.valueOf(car.getId())});
                Toast.makeText(mCtx, "수정완료", Toast.LENGTH_SHORT).show();
                reloadCarsFromDatabase();

                dialog.dismiss();
            }
        });
    }

    private void reloadCarsFromDatabase() {
        Cursor cursorCars = mDatabase.rawQuery("SELECT * FROM CARLIST", null);
        if (cursorCars.moveToFirst()) {
            carList.clear();
            do {
                carList.add(new Car_Info(
                        cursorCars.getInt(0),
                        cursorCars.getString(1),
                        cursorCars.getString(2),
                        cursorCars.getString(3),
                        cursorCars.getString(4)
                ));
            } while (cursorCars.moveToNext());
        }
        cursorCars.close();
        notifyDataSetChanged();
    }

}
