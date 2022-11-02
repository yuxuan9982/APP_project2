package com.example.puzzlegame.slice;

import com.example.puzzlegame.DynamicPage;
import com.example.puzzlegame.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.*;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.global.resource.NotExistException;
import ohos.global.resource.Resource;

import java.io.IOException;

public class Init_pageSlice extends AbilitySlice {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_init_page);
        Button begin=(Button) findComponentById(ResourceTable.Id_into);
        begin.setClickedListener(component -> {
            Intent intent1=new Intent();
            AbilitySlice slice=new MainAbilitySlice();
            present(slice,intent1);
        });
        Button explain=(Button) findComponentById(ResourceTable.Id_explain);
        explain.setClickedListener(com->{
            CommonDialog cd=new CommonDialog(this);
            cd.setAutoClosable(true);
            DirectionalLayout dl=build_dl(0);
            ScrollView sv=new ScrollView(this);
            sv.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
            sv.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);

            Text content= new Text(this);
            set_but_back(content,104,0,254);
            content.setTextSize(50);content.setMultipleLine(true);
            content.setText("    在本游戏中，你需要先进入游戏，然后选择难度，必须选择了难度以后才能进行拼图，否则不能进行" +
                    "拼图。\n     你可以使用相册和手机拍照来得到你想要的照片，但是这个前提是你的手机必须具备相机功能。\n " +
                    "    难度有2X2到10X10的灵活选择，但是作者强烈建议您不要选择4X4以上的难度，因为这样的难度已经过于困难。\n" +
                    "    本游戏有两种动画效果，分别是旋转效果和平移效果，默认选择为平移效果。\n" +
                    "    本游戏并不保证一定有解，因此如果你能看出该排列没有解，可以直接放弃！\n" +
                    "    由于作者使用的是directional layout，作者仅仅做好了在P40机型的界面，在非P40分辨率的机型上可能会出现视图" +
                    "方面的问题。\n" +
                    "    此外，作者设计了一个很小的彩蛋，如果你不选择任何图片，那么问号也可以是图片。");

            sv.addComponent(content);
            dl.addComponent(sv);
            cd.setContentCustomComponent(dl);
            cd.show();
        });
        Button info=(Button) findComponentById(ResourceTable.Id_personal);
        info.setClickedListener(com->{
            CommonDialog cd=new CommonDialog(this);
            cd.setAutoClosable(true);
            DirectionalLayout dl=build_dl(0);

            Text title= new Text(this);
            //set_but_back(title,104,0,254);
            title.setTextSize(50);title.setMultipleLine(true);
            title.setText("信息说明");

            Text content= new Text(this);
            content.setTextSize(50);content.setMultipleLine(true);
            content.setMultipleLine(true);
            content.setText("游戏名：拼图游戏\n别名：数字华容道\n作者：于轩");

            Text end=new Text(this);
            end.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);
            end.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
            end.setTextSize(50);
            end.setText("ver:--1.05");
            dl.setPadding(20,20,20,20);
            dl.addComponent(title);dl.addComponent(content);dl.addComponent(end);
            set_but_back(dl,230,231,249);
            cd.setContentCustomComponent(dl);
            cd.show();
        });
    }
    public void set_but_back(Component component,int r,int g,int b){
        ShapeElement element=new ShapeElement();
        element.setShape(ShapeElement.RECTANGLE);
        element.setCornerRadius(30);
        element.setRgbColor(new RgbColor(r,g,b));
        element.setStroke(10,new RgbColor(0,0,255));
        component.setBackground(element);
    }
    public DirectionalLayout build_dl(int type){
        DirectionalLayout dl=new DirectionalLayout(this);
        if(type==0)dl.setOrientation(Component.VERTICAL);else dl.setOrientation(Component.HORIZONTAL);
        dl.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        dl.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        dl.setAlignment(LayoutAlignment.CENTER);
        return dl;
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
