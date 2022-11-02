package com.example.puzzlegame.slice;

import com.example.puzzlegame.ImageListProvider;
import com.example.puzzlegame.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.ability.DataAbilityHelper;
import ohos.aafwk.ability.DataAbilityRemoteException;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorProperty;
import ohos.agp.components.*;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.dialog.ToastDialog;
import ohos.data.rdb.ValuesBucket;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.device.Camera;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.ImageInfo;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;
import ohos.media.photokit.metadata.AVStorage;
import ohos.utils.net.Uri;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.*;

public class MainAbilitySlice extends AbilitySlice  {
    Image cur;Integer cur_src;
    PixelMap mp[][];Image pic[][];
    Image emp;
    Integer row_n,col_m;
    private int RequestCode=1123;
    Uri imageUri1;
    //animation part
    //static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00201, "LoginAbilitySlice");
//    public void init(){
//        pic[0][0]=(Image) findComponentById(ResourceTable.Id_img1);
//        pic[0][1]=(Image) findComponentById(ResourceTable.Id_img2);
//        pic[0][2]=(Image) findComponentById(ResourceTable.Id_img3);
//        pic[1][0]=(Image) findComponentById(ResourceTable.Id_img4);
//        pic[1][1]=(Image) findComponentById(ResourceTable.Id_img5);
//        pic[1][2]=(Image) findComponentById(ResourceTable.Id_img6);
//        pic[2][0]=(Image) findComponentById(ResourceTable.Id_img7);
//        pic[2][1]=(Image) findComponentById(ResourceTable.Id_img8);
//        pic[2][2]=(Image) findComponentById(ResourceTable.Id_img9);
//        pic[2][3]=(Image) findComponentById(ResourceTable.Id_img10);
//        emp=pic[cut_num-1][cut_num-1];empi=cut_num-1;empj=cut_num-1;
//    }
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
//        cut_num=3;
//        mp=new PixelMap[cut_num][cut_num+1];
//        pic=new Image[cut_num][cut_num+1];
//        //get the current picture
//        init();
        cur=(Image)findComponentById(ResourceTable.Id_current);

        //build listContainer
        ListContainer lcImgList = (ListContainer) findComponentById(ResourceTable.Id_ListContainer);
        List<Integer> imgList= getImagelist();
        //provider needed
        ImageListProvider provider= new ImageListProvider(imgList,this);
        provider.setListener(new ImageListProvider.ClickedListener() {
            @Override
            public void click(int pos) {
                cur.setPixelMap(imgList.get(pos));
                cur_src=imgList.get(pos);
//                PixelMap pix=cur.getPixelMap();
//                cut_pic(pix);
            }
        });
        lcImgList.setItemProvider(provider);
        row_n=col_m=-1;
        Button begin=(Button) findComponentById(ResourceTable.Id_begin);
        begin.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                if(row_n==-1||col_m==-1){
                    ToastDialog td=new ToastDialog(getContext());
                    td.setText("你还没有选择难度");
                    td.setAutoClosable(true);
                    td.show();
                }else{
                    Intent in1=new Intent();
                    AbilitySlice slice= new DynamicPageSlice();
                    in1.setParam("row_n",row_n);
                    in1.setParam("col_m",col_m);
                    in1.setParam("imgsrc",cur_src);
                    present(slice,in1);
                }
            }
        });
        Button select=(Button) findComponentById(ResourceTable.Id_select);
        select.setClickedListener(this::Select);

        Button photo_album=(Button) findComponentById(ResourceTable.Id_PhotoAlbum);
        //获取权限
        requestPermissionsFromUser(new String[]{"ohos.permission.READ_USER_STORAGE","ohos.permission.CAMERA"},RequestCode);
        photo_album.setClickedListener(com->{
            select_pic();
        });

        Button takephoto=(Button) findComponentById(ResourceTable.Id_TakePhoto);
        takephoto.setClickedListener(cmp->{
            take_photo();
        });
    }
    private void take_photo(){
//        Intent intent=new Intent();
//        Operation opt=new Intent.OperationBuilder().withAction("android.media.action.IMAGE_CAPTURE").build();
//        intent.setOperation(opt);
//        startAbilityForResult(intent,RequestCode);
        ValuesBucket valuesBucket = new ValuesBucket();
        valuesBucket.putString("relative_path", "DCIM/Camera/");
        valuesBucket.putString(AVStorage.Images.Media.MIME_TYPE, "image/JPEG");
        DataAbilityHelper helper = DataAbilityHelper.creator(getContext());
        try {
            int id = helper.insert(AVStorage.Images.Media.EXTERNAL_DATA_ABILITY_URI, valuesBucket);
            imageUri1 = Uri.appendEncodedPathToUri(AVStorage.Images.Media.EXTERNAL_DATA_ABILITY_URI, String.valueOf(id));
            Intent intent = new Intent();
            Operation operation = new Intent.OperationBuilder()
                    .withAction("android.media.action.IMAGE_CAPTURE")
                    .build();
            intent.setOperation(operation);
            intent.setParam(AVStorage.Images.Media.OUTPUT, imageUri1);
            startAbilityForResult(intent, 1);

        } catch (DataAbilityRemoteException e) {
            e.printStackTrace();
        }
    }
    private void select_pic(){
        Intent intent=new Intent();
        Operation opt=new Intent.OperationBuilder().withAction("android.intent.action.GET_CONTENT").build();
        intent.setOperation(opt);
        intent.addFlags(Intent.FLAG_NOT_OHOS_COMPONENT);
        intent.setType("image/*");
        startAbilityForResult(intent,RequestCode);
    }

    @Override
    protected void onAbilityResult(int requestCode, int resultCode, Intent resultData) {
        if(requestCode==RequestCode){
            String ImgUri=null;
            try{
                ImgUri=resultData.getUriString();
            }catch(Exception e) {
                return ;
            }

            DataAbilityHelper helper= DataAbilityHelper.creator(getContext());
            ImageSource ims=null;
            String ImgId=null;
            if(ImgUri.lastIndexOf("%3A")!=-1){
                ImgId= ImgUri.substring(ImgUri.lastIndexOf("%3A")+3);
            }else{
                ImgId= ImgUri.substring(ImgUri.lastIndexOf('/')+1);
            }
            Uri uri= Uri.appendEncodedPathToUri(AVStorage.Images.Media.EXTERNAL_DATA_ABILITY_URI,ImgId);
            try{
                FileDescriptor fd= helper.openFile(uri,"r");
                ims= ImageSource.create(fd,null);
                PixelMap px= ims.createPixelmap(null);
                cur.setScaleMode(Image.ScaleMode.STRETCH);
                cur.setPixelMap(px);
            } catch (DataAbilityRemoteException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                if(ims!=null){
                    ims.release();
                }
            }
        }else if(requestCode==1){
            DataAbilityHelper helper=DataAbilityHelper.creator(getContext());
            Uri uri= imageUri1;
            try {
                FileDescriptor fd= helper.openFile(uri,"r");
                ImageSource ims= ImageSource.create(fd,null);
                PixelMap px= ims.createPixelmap(null);
                cur.setScaleMode(Image.ScaleMode.STRETCH);
                cur.setPixelMap(px);
            } catch (DataAbilityRemoteException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            ToastDialog td=new ToastDialog(this);
            td.setText("成功！！！！！这个功能网上搜都搜不到，太难了哭死。。yx");
            td.show();
        }
    }

    public void Select(Component component){
        CommonDialog cd= new CommonDialog(this);
        cd.setCornerRadius(40);
        cd.setAutoClosable(true);
//        DirectionalLayout dl= (DirectionalLayout) LayoutScatter.getInstance(this).parse(ResourceTable.Layout_select_difficulty,null,false);
//        RadioContainer rc=(RadioContainer) dl.findComponentById(ResourceTable.Id_ratio);
//        //
//        rc.setMarkChangedListener(new RadioContainer.CheckedStateChangedListener() {
//            @Override
//            public void onCheckedChanged(RadioContainer radioContainer, int i) {
//                RadioButton but=(RadioButton) radioContainer.getComponentAt(i);
//                if(but.isChecked()){
//                    cut_num=i+2;
//                }else{
//                    cut_num=-1;
//                }
//                cd.destroy();
//            }
//        });
        DirectionalLayout dl=new DirectionalLayout(this);
        DirectionalLayout ddl1=new DirectionalLayout(this);
        DirectionalLayout ddl2=new DirectionalLayout(this);
        DirectionalLayout ddl3=new DirectionalLayout(this);

        dl.setOrientation(Component.VERTICAL);
        dl.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        dl.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        dl.setAlignment(LayoutAlignment.CENTER);

        ddl1.setOrientation(Component.HORIZONTAL);
        ddl1.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        ddl1.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        ddl1.setAlignment(LayoutAlignment.CENTER);

        ddl2.setOrientation(Component.HORIZONTAL);
        ddl2.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        ddl2.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        ddl2.setAlignment(LayoutAlignment.CENTER);

        ddl3.setOrientation(Component.HORIZONTAL);
        ddl3.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        ddl3.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        ddl3.setAlignment(LayoutAlignment.CENTER);

        Button row=new Button(this);
        Button col=new Button(this);
        col.setTextSize(50);col.setText("列数");
        row.setTextSize(50);row.setText("行数");
        row.setMarginLeft(200);col.setMarginLeft(400);
        ddl1.addComponent(row);ddl1.addComponent(col);

        Picker picker=new Picker(this);
        Picker picker2=new Picker(this);
        picker.setMaxValue(10);picker2.setMaxValue(10);
        picker.setMinValue(2);picker2.setMinValue(2);
        picker.setNormalTextSize(50);picker2.setNormalTextSize(50);
        picker.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);picker2.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        picker.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);picker2.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        picker.setSelectedTextSize(50);picker2.setSelectedTextSize(50);
        picker.setMarginLeft(200);picker2.setMarginLeft(400);

        ddl2.addComponent(picker);ddl2.addComponent(picker2);



        Button ack=new Button(this);
        Button ret=new Button(this);
        ack.setTextSize(50);ack.setText("确认");
        ret.setTextSize(50);ret.setText("返回");
        ret.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                row_n=col_m=-1;cd.destroy();
            }
        });
        ack.setClickedListener((Component cmp)->{
            row_n=picker.getValue();col_m=picker2.getValue();
            cd.destroy();
        });
        ret.setMarginLeft(200);ack.setMarginLeft(400);
        ddl3.addComponent(ret);ddl3.addComponent(ack);



        dl.addComponent(ddl1);dl.addComponent(ddl2);dl.addComponent(ddl3);
        cd.setContentCustomComponent(dl);
        cd.show();
    }
    public List<Integer> getImagelist(){
        List<Integer> lst = new ArrayList<>();
        lst.add(ResourceTable.Media_a6);
        lst.add(ResourceTable.Media_a7);
        lst.add(ResourceTable.Media_a8);
        lst.add(ResourceTable.Media_a9);
        lst.add(ResourceTable.Media_a10);
        lst.add(ResourceTable.Media_a11);
        lst.add(ResourceTable.Media_p3);
        lst.add(ResourceTable.Media_a1);
        lst.add(ResourceTable.Media_a2);
        lst.add(ResourceTable.Media_a3);
        lst.add(ResourceTable.Media_a4);
        lst.add(ResourceTable.Media_a5);
        return lst;
    }
    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
