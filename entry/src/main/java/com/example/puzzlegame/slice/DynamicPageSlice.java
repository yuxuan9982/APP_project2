package com.example.puzzlegame.slice;

import com.example.puzzlegame.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.animation.Animator;
import ohos.agp.animation.AnimatorGroup;
import ohos.agp.animation.AnimatorProperty;
import ohos.agp.colors.RgbColor;
import ohos.agp.components.*;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.components.element.ShapeElement;
import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.render.Texture;
import ohos.agp.utils.Color;
import ohos.agp.utils.LayoutAlignment;
import ohos.agp.window.dialog.CommonDialog;
import ohos.agp.window.dialog.IDialog;
import ohos.global.resource.NotExistException;
import ohos.global.resource.Resource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.ImageInfo;
import ohos.media.image.common.Rect;
import ohos.media.image.common.Size;

import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

public class DynamicPageSlice extends AbilitySlice implements Component.ClickedListener {
    Image cur;
    int row_sz,col_sz,row,col,picsz_r,picsz_c,step=0;
    PixelMap mp[][];Image pic[][];
    Integer pos[][];
    Image emp;
    TickTimer timer;long startTime=0,passTime=0,fg=0;
    int nowi,nowj,empi,empj;
    Text sp;
    Picker picker;
    public void cut_pic(PixelMap pixelMap){
        ImageInfo info=pixelMap.getImageInfo();
        //make initialization
        PixelMap.InitializationOptions opt=new PixelMap.InitializationOptions();
        int w=Math.min(info.size.height,info.size.width);
        opt.size=new Size();
        opt.size.width=opt.size.height=w;
        opt.pixelFormat=info.pixelFormat;
        opt.editable=true;
        //get cutted size
        Rect rect=new Rect();
        rect.minX=0;rect.minY=0;rect.width=col_sz;rect.height=row_sz;
        PixelMap pixelMap1= PixelMap.create(pixelMap,rect,opt);
        cur.setPixelMap(pixelMap1);
        info=pixelMap1.getImageInfo();
        row_sz=(int) info.size.height/row;
        col_sz=(int) info.size.width/col;
        int sz=Math.max(row_sz,col_sz);
        // split the picture
        int t=0;
        int pm_px=AttrHelper.vp2px(getContext().getResourceManager().getDeviceCapability().width,this);
        for(int i=0;i<row;i++)
            for(int j=0;j<col;j++){
                Rect r1=new Rect();
                r1.height=row_sz;r1.width=col_sz;
                r1.minX=j*col_sz;r1.minY=i*row_sz;
                PixelMap tmp=PixelMap.create(pixelMap1,r1,opt);
                Canvas canvas= new Canvas(new Texture(tmp));
                Paint paint= new Paint();
                paint.setTextSize(sz);
                paint.setColor(Color.RED);
                canvas.drawText(paint,""+(++t),col_sz,row_sz*2);
                mp[i][j]=tmp;
            }
        random_shuffle();
    }
    public void random_shuffle(){
        //make a random shuffle
        Vector<Integer> lst=new Vector<Integer>() ;
        for(int i=0;i<=row*col-1;i++)lst.add(i);
        Collections.shuffle(lst);
        for(int i=0;i<=row*col-1;i++){
            pic[i/col][i%col+(i==row*col-1?1:0)].setPixelMap(mp[lst.get(i)/col][lst.get(i)%col]);
            pos[i/col][i%col+(i==row*col-1?1:0)]=lst.get(i);
        }
        empi=row-1;empj=col-1;emp=pic[empi][empj];
        pic[empi][empj].setPixelMap(ResourceTable.Media_icon);
        //propertyAnimationImage = findComponentById(ResourceTable.Id_img1);
    }
    public DirectionalLayout build_dl(int type){
        DirectionalLayout dl=new DirectionalLayout(this);
        if(type==0)dl.setOrientation(Component.VERTICAL);else dl.setOrientation(Component.HORIZONTAL);
        dl.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        dl.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        dl.setAlignment(LayoutAlignment.CENTER);
        return dl;
    }
    public void set_Background(Component component,int id){
        try {
            Resource resource= getResourceManager().getResource(id);
            PixelMapElement pixelMapElement=new PixelMapElement(resource);
            component.setBackground(pixelMapElement);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NotExistException e) {
            e.printStackTrace();
        }
    }
    public void set_but_back(Component component,int r,int g,int b){
        ShapeElement element=new ShapeElement();
        element.setShape(ShapeElement.RECTANGLE);
        element.setCornerRadius(30);
        element.setRgbColor(new RgbColor(r,g,b));
        element.setStroke(10,new RgbColor(0,0,255));
        component.setBackground(element);
    }
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        //super.setUIContent(ResourceTable.Layout_DynamicPage);

        DirectionalLayout dl=new DirectionalLayout(this);
        //set back_ground
        set_Background(dl,ResourceTable.Media_bg3);
        dl.setOrientation(Component.VERTICAL);
        dl.setWidth(ComponentContainer.LayoutConfig.MATCH_PARENT);
        dl.setHeight(ComponentContainer.LayoutConfig.MATCH_PARENT);
        dl.setAlignment(LayoutAlignment.TOP);
        row= intent.getIntParam("row_n",3);
        col= intent.getIntParam("col_m",3);
        cur=new Image(this);
        cur.setScaleMode(Image.ScaleMode.STRETCH);
        cur.setHeight(AttrHelper.vp2px(200,this));
        cur.setWidth(AttrHelper.vp2px(200,this));
        //cur.setPixelMap(intent.getIntParam("imgsrc",ResourceTable.Media_question));
        cur.setPixelMap( ( (Image)findComponentById(ResourceTable.Id_current) ).getPixelMap());
        //cur.setMarginTop(100);cur.setMarginLeft(100);


        pic= new Image[row+1][col+1];
        mp= new PixelMap[row+1][col+1];//forget to init this before
        pos = new Integer[row+1][col+1];
        for(int i=0;i<=row;i++)for(int j=0;j<=col;j++)
            pos[i][j]=-1;
        picsz_r= (int)250/(row);picsz_c=(int) 250/(col);

        DirectionalLayout d1= build_dl(1);
        d1.setMarginLeft(100);d1.setMarginTop(100);
        d1.addComponent(cur);

        DirectionalLayout dd1= build_dl(0);
        dd1.setMarginLeft(40);
        DirectionalLayout ddd1= build_dl(1);
        int sz=70;
        //set text
        Text txt=new Text(this);
        txt.setText("耗时: ");txt.setTextSize(sz);
        //set timer
        timer= new TickTimer(this);
        timer.setCountDown(false);
        timer.setTextSize(sz);

        ddd1.addComponent(txt);
        ddd1.addComponent(timer);

        Button but1=new Button(this);
        but1.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);but1.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        but1.setText("难度: "+row+"X"+col);but1.setTextSize(sz);

        dd1.setAlignment(LayoutAlignment.LEFT);
        dd1.addComponent(ddd1);
        dd1.addComponent(but1);

        sp=new Text(this);
        sp.setTextSize(sz);sp.setText("步数："+step);
        dd1.addComponent(sp);
        //设置背景颜色
        set_but_back(dd1,0,204,255);
        dd1.setPadding(20,20,20,20);

        d1.addComponent(dd1);
        dl.addComponent(d1);
        for(int i=0;i<=row;i++)for(int j=0;j<=col;j++){
            pic[i][j]=new Image(this);  //without this null exception
            pic[i][j].setWidth(AttrHelper.vp2px(picsz_c,this));
            pic[i][j].setHeight(AttrHelper.vp2px(picsz_r,this));
            pic[i][j].setScaleMode(Image.ScaleMode.STRETCH);
            //pic[i][j].setPadding(15,15,15,15);
            if(j!=0)pic[i][j].setMarginLeft(15);
            pic[i][j].setMarginTop(15);
            pic[i][j].setPixelMap(ResourceTable.Media_icon);
            if(i==row-1)pic[i][j].setMarginBottom(30);
            pic[i][j].setClickedListener(this);
        }
        pic[row-1][col].setMarginRight(30);
        //pic[cut_num][cut_num+1].setClickedListener(this);
        DirectionalLayout nd=build_dl(0);
        nd.setMarginTop(100);
        nd.setAlignment(LayoutAlignment.TOP);
        PixelMap px=cur.getPixelMap();
        cut_pic(px);
        for(int i=0;i<row;i++){
            DirectionalLayout tmpd= new DirectionalLayout(this);
            tmpd.setMarginLeft(100);
            tmpd.setOrientation(Component.HORIZONTAL);
            tmpd.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);
            tmpd.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
            //tmpd.setAlignment(LayoutAlignment.CENTER);
            for(int j=0;j<col;j++){
                tmpd.addComponent(pic[i][j]);
            }
            if(i==row-1)tmpd.addComponent(pic[i][col]);
            nd.addComponent(tmpd);
        }
        set_but_back(nd,230,231,249);
        //nd.setPadding(20,20,20,20);
        dl.addComponent(nd);

        DirectionalLayout d2= new DirectionalLayout(this);
        d2.setMarginLeft(100);d2.setMarginTop(100);
        d2.setOrientation(Component.HORIZONTAL);
        d2.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        d2.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        d2.setAlignment(LayoutAlignment.HORIZONTAL_CENTER);
        Button ret= new Button(this);
        ret.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        ret.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);ret.setText("提前结束");
        ret.setTextSize(50);
        ret.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                CommonDialog cd= new CommonDialog(getContext());
                ///cd.setSize(800, 600);
                cd.setAlignment(LayoutAlignment.CENTER);
                cd.setTitleText("返回");
                cd.setContentText("你真的做不下去了吗？如果是，请点击确认按钮");
                cd.setButton(0, "是", new IDialog.ClickedListener() {
                    @Override
                    public void onClick(IDialog iDialog, int i ) {
                        cd.destroy();terminate();
                    }
                });
                cd.setButton(1, "否", new IDialog.ClickedListener() {
                    @Override
                    public void onClick(IDialog iDialog, int i) {
                        cd.destroy();
                    }
                });
                cd.show();
            }
        });
        ret.setPadding(20,20,20,20);
        set_but_back(ret,0,204,255);
        d2.addComponent(ret);

        Button reset=new Button(this);
        reset.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);reset.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        reset.setText("重置");reset.setTextSize(100);reset.setMarginLeft(100);
        reset.setClickedListener(new Component.ClickedListener() {
            @Override
            public void onClick(Component component) {
                startTime = System.currentTimeMillis();
                timer.setBaseTime(startTime - passTime);
                timer.stop();fg=0;step=0;update();
                //cut_pic(cur.getPixelMap());
                random_shuffle();
            }
        });
        reset.setPadding(20,20,20,20);
        set_but_back(reset,0,204,255);
        d2.addComponent(reset);

        DirectionalLayout dd2=build_dl(0);
        Text title=new Text(this);
        title.setTextSize(50);title.setText("动画效果");
        title.setTextColor(Color.YELLOW);
        title.setMarginTop(20);
        dd2.addComponent(title);
        picker=new Picker(this);
        picker.setMinValue(0);picker.setMaxValue(1);
        picker.setNormalTextSize(50);picker.setSelectedTextSize(50);
        picker.setDisplayedData(new String[]{"旋转效果","平移效果"});
        picker.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);
        picker.setHeight(300);

        dd2.setMarginLeft(100);

        set_but_back(picker,103,0,204);
        dd2.addComponent(picker);
        //set_but_back(dd2,103,0,204);
        d2.addComponent(dd2);

        dl.addComponent(d2);


        super.setUIContent(dl);
    }
    public void update(){
        sp.setText("步数："+step);
    }
    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
    public void find(Component component){
        for(int i=0;i<=row;i++)
            for(int j=0;j<=col;j++){
                if(component==pic[i][j]){
                    nowi=i;nowj=j;return ;
                }
            }
    }
    public boolean check(){
        if(Math.abs(nowi-empi)+Math.abs(nowj-empj)==1)return true;
        return false;
    }
    public boolean finished(){
        for(int i=0;i<row;i++)
            for(int j=0;j<col;j++)
                if(pos[i][j]!=(i)*col+j)return false;
        return true;
    }
    float s_x,s_y,f_x,f_y;
    @Override
    public void onClick(Component component) {
        if(fg==0){
            startTime = System.currentTimeMillis();
            timer.setBaseTime(startTime - passTime);
            timer.start();fg=1;
        }
        find(component);
        if(check()){
            ++step;update();
            Image img1= (Image) component;
            Image img2= emp;
            PixelMap p1=img1.getPixelMap();
            PixelMap p2=img2.getPixelMap();
            {
                Integer tmp=pos[nowi][nowj];
                pos[nowi][nowj]=pos[empi][empj];
                pos[empi][empj]=tmp;
            }
            if(finished()){
                double t = (System.currentTimeMillis() - startTime) / 1000.0;
                String s = t + "";
                timer.stop();
                CommonDialog cd= new CommonDialog(getContext());
                DirectionalLayout dl=build_dl(0);
                dl.setAlignment(LayoutAlignment.CENTER);
                Text title=new Text(this);title.setText("拼图完成！");title.setTextSize(50);
                Text content=new Text(this);content.setText("恭喜你，成功的拼完了整张图片！用时为："+s+"s"+"，完成步数为："+step+"步");
                content.setTextSize(50);
                content.setMultipleLine(true);content.setMaxTextLines(4);
                Button but=new Button(this);but.setText("返回");but.setWidth(ComponentContainer.LayoutConfig.MATCH_CONTENT);but.setHeight(ComponentContainer.LayoutConfig.MATCH_CONTENT);
                but.setTextSize(50);
                but.setClickedListener(new Component.ClickedListener() {
                    @Override
                    public void onClick(Component component) {
                        cd.destroy();terminate();
                    }
                });
                dl.addComponent(title);dl.addComponent(content);dl.addComponent(but);
                cd.setContentCustomComponent(dl);
                cd.show();
                //t.setAutoClosable(true);
            }
            //play(img2);play(img1);
            //img1.setPixelMap(p2);img2.setPixelMap(p1);
            if(picker.getValue()==1){
                move(img1,img2);
            }else{
                play(img1);play(img2);
                img1.setPixelMap(p2);img2.setPixelMap(p1);
            }
            emp=img1;empi=nowi;empj=nowj;
        }
    }
    public void move(Image c1,Image c2){
        AnimatorProperty p1=c1.createAnimatorProperty(),p2=c2.createAnimatorProperty();
        AnimatorGroup ag=new AnimatorGroup();
        ag.setStateChangedListener(new Animator.StateChangedListener() {
            @Override
            public void onStart(Animator animator) {
            }
            @Override
            public void onStop(Animator animator) {
            }
            @Override
            public void onCancel(Animator animator) {
            }
            @Override
            public void onEnd(Animator animator) {
                c1.setContentPositionX(s_x);c1.setContentPositionY(s_y);
                c2.setContentPositionX(f_x);c2.setContentPositionY(f_y);
                PixelMap pi1=c1.getPixelMap();
                PixelMap pi2=c2.getPixelMap();
                c2.setPixelMap(pi1);c1.setPixelMap(pi2);
            }
            @Override
            public void onPause(Animator animator) {
            }
            @Override
            public void onResume(Animator animator) {
            }
        });
        ag.setDuration(1000);
        s_x=c1.getContentPositionX();s_y=c1.getContentPositionY();
        f_x=c2.getContentPositionX();f_y=c2.getContentPositionY();
        int fg=0;
        if(s_x==f_x) {
            if (nowi < empi) fg = 1;
            else fg = -1;
            if (nowi < empi) {
                p1.moveFromY(f_y).moveToY(f_y + 15 + picsz_r);
                p2.moveFromY(f_y).moveToY(f_y - 15 - picsz_r);
            } else {
                p2.moveFromY(f_y).moveToY(f_y + 15 + picsz_r);
                p1.moveFromY(f_y).moveToY(f_y - 15 - picsz_r);
            }
        } else{
            p1.moveFromX(s_x).moveToX(f_x);
            p2.moveFromX(f_x).moveToX(s_x);
        }
        ag.runParallel(p1,p2);

        ag.start();
    }
    public void play(Component component){
        Component propertyAnimationImage=component;
        AnimatorProperty animator = propertyAnimationImage.createAnimatorProperty();
        propertyAnimationImage.setRotation(0);
        animator.setCurveType(Animator.CurveType.ANTICIPATE_OVERSHOOT);
        animator.rotate(360);
        animator.setDuration(500);
        animator.setLoopedCount(2);
        //animator.setStateChangedListener();
        animator.start();
    }
}
