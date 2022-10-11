package com.example.makeit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CameraFragment extends Fragment implements View.OnClickListener {
    ImageView iv_picture;
    Button btn_basic, btn_ar, btn_color, btn_save;
    Uri photoUri;
    private String mCurrentPhotoPath;

    private static final String TAG = "save";

    public static final int REQUEST_TAKE_PHOTO = 10;
    public static final int REQUEST_PERMISSION = 11;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_camera, container, false);

        iv_picture = viewGroup.findViewById(R.id.iv_picture);
        btn_basic = viewGroup.findViewById(R.id.btn_basic);
        btn_ar = viewGroup.findViewById(R.id.btn_ar);
        btn_color = viewGroup.findViewById(R.id.btn_color);
        btn_save = viewGroup.findViewById(R.id.btn_save);

        checkPermission(); //권한체크

        loadImgArr();

        //촬영
        btn_basic.setOnClickListener(v -> captureCamera());

        //저장
        btn_save.setOnClickListener(v -> {

            try {

                BitmapDrawable drawable = (BitmapDrawable) iv_picture.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                //찍은 사진이 없으면
                if (bitmap == null) {
                    Toast.makeText(getActivity(), "저장할 사진이 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    //저장
                    saveImg();
                    mCurrentPhotoPath = ""; //initialize
                }

            } catch (Exception e) {
                Log.w(TAG, "SAVE ERROR!", e);
            }
        });

        btn_ar.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

            }
        });

        btn_color.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

            }
        });
        return viewGroup;
    }

    private void captureCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //인텐트를 처리 할 카메라 액티비티가 있는지 확인
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

            //촬영한 사진을 저장할 파일 생성
            File photoFile = null;

            try {
                //임시로 사용할 파일이므로 경로는 캐시폴더로
                File tempDir = getActivity().getCacheDir();

                //임시촬영파일 세팅
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "Capture_" + timeStamp + "_";

                File tempImage = File.createTempFile(
                        imageFileName, //파일이름
                        ".jpg", //파일형식
                        tempDir //경로
                );
                Log.d(TAG, imageFileName);
                Log.d(TAG, String.valueOf(tempDir));

                //ACTION_VIEW 인텐트를 사용할 경로 (임시파일의 경로)
                mCurrentPhotoPath = tempImage.getAbsolutePath();

                photoFile = tempImage;

            } catch (IOException e) {
                //에러 로그는 이렇게 관리하는 편이 좋다.
                Log.w(TAG, "파일 생성 에러!", e);
            }

            //파일이 정상적으로 생성되었다면 계속 진행
            if (photoFile != null) {
                //Uri 가져오기
                Uri photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                        getActivity().getPackageName() + ".fileprovider",
                        photoFile);
                //인텐트에 Uri담기
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                //인텐트 실행
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    //이미지저장 메소드
    private void saveImg() {

        try {
            //저장할 파일 경로
            File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),"Make_It!");
            if (!storageDir.exists()) //폴더가 없으면 생성
                storageDir.mkdirs();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = "Capture_" + timeStamp + ".jpg";

            //기존에 파일이 있을 경우
            File file = new File(storageDir, filename);
            boolean deleted = file.delete();
            Log.w(TAG, "Delete Dup Check : " + deleted);
            FileOutputStream output = null;

            try {
                output = new FileOutputStream(file); //파라미터로 보낸 파일을 쓰기 위한 스트림 생성
                BitmapDrawable drawable = (BitmapDrawable) iv_picture.getDrawable();
                Bitmap bitmap = drawable.getBitmap();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, output); //compress 함수로 스트림에 비트맵을 저장
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    assert output != null;
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Log.e(TAG, "Captured Saved");
            Toast.makeText(getActivity(), "Capture Saved ", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.w(TAG, "Capture Saving Error!", e);
            Toast.makeText(getActivity(), "Save failed", Toast.LENGTH_SHORT).show();

        }
    }

    private void loadImgArr() {
        try {

            File storageDir = new File(getActivity().getFilesDir() + "/capture");
            //getFilesDir()에서 제공한 디렉터리에 저장한 파일은 사용자가 앱을 제거할 때 삭제
            String filename = "캡쳐파일" + ".jpg";

            //그 전에 찍었던 사진이 iv_picture에 계속 뜨게 함
            /*File file = new File(storageDir, filename);
            Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
            iv_picture.setImageBitmap(bitmap);*/

        } catch (Exception e) {
            Log.w(TAG, "Capture loading Error!", e);
            Toast.makeText(getActivity(), "load failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        try {
            //after capture
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO: {
                    if (resultCode == -1) {

                        File file = new File(mCurrentPhotoPath);
                        Bitmap bitmap = MediaStore.Images.Media
                                .getBitmap(getActivity().getContentResolver(), Uri.fromFile(file));

                        if (bitmap != null) {
                            ExifInterface ei = new ExifInterface(mCurrentPhotoPath);
                            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                    ExifInterface.ORIENTATION_UNDEFINED);

                            //사진해상도가 너무 높으면 비트맵으로 로딩
                            /*BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 8; //8분의 1크기로 비트맵 객체 생성
                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);*/

                            Bitmap rotatedBitmap = null;
                            switch (orientation) {

                                case ExifInterface.ORIENTATION_ROTATE_90:
                                    rotatedBitmap = rotateImage(bitmap, 90);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_180:
                                    rotatedBitmap = rotateImage(bitmap, 180);
                                    break;

                                case ExifInterface.ORIENTATION_ROTATE_270:
                                    rotatedBitmap = rotateImage(bitmap, 270);
                                    break;

                                case ExifInterface.ORIENTATION_NORMAL:
                                default:
                                    rotatedBitmap = bitmap;
                            }

                            //Rotate한 bitmap을 ImageView에 저장
                            iv_picture.setImageBitmap(rotatedBitmap);

                        }
                    }
                    break;
                }
            }

        } catch (Exception e) {
            Log.w(TAG, "onActivityResult Error !", e);
        }
    }

    //카메라에 맞게 이미지 로테이션
    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    //카메라 권한-----------------------------------------------------------------------------------------------------------------
    @Override
    public void onResume() {
        super.onResume();
        checkPermission(); //권한체크
    }

    //권한 확인
    public void checkPermission() {
        int permissionCamera = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.CAMERA);
        int permissionRead = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWrite = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        //권한이 없으면 권한 요청
        if (permissionCamera != PackageManager.PERMISSION_GRANTED
                || permissionRead != PackageManager.PERMISSION_GRANTED
                || permissionWrite != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA)) {
                Toast.makeText(getActivity(), "이 앱을 실행하기 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }

            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION: {
                //권한이 취소되면 result 배열은 비어있다.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(getActivity(), "권한 확인", Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(getActivity(), "권한 없음", Toast.LENGTH_LONG).show();
                    getActivity().finish(); //권한이 없으면 앱 종료
                }
            }
        }
    }
    //--------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onClick(View view) {
        //implements your things
    }
}