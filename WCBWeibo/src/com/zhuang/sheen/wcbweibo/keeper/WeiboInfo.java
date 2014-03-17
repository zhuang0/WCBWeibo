package com.zhuang.sheen.wcbweibo.keeper;


public class WeiboInfo{
	//文章id
    private String id;
    public String getId(){
        return id;
    }
    public void setId(String id){
        this.id=id;
    }
    //发布人id
    private String userId;
    public String getUserId(){
        return userId;
    }
    public void setUserId(String userId){
        this.userId=userId;
    }
    
    //发布人名字
    private String userName;
    public String getUserName(){
        return userName;
    }
    public void setUserName(String userName){
        this.userName=userName;
    }
    
    //发布人头像
    private String userIcon;
    public String getUserIcon(){
        return userIcon;
    }
    public void setUserIcon(String userIcon){
        this.userIcon=userIcon;
    }
    
    //发布时间
    private String time;
    public String getTime(){
        return time;
    }
    public void setTime(String time)
    {
        this.time=time;
    }
    
    //是否有图片
    private Boolean haveImage=false;
    public Boolean getHaveImage(){
        return haveImage;
    }
    public void setHaveImage(Boolean haveImage){
        this.haveImage=haveImage;
    }
    
    //是否有大图
    private Boolean haveLargeImage=false;
    public Boolean getHaveLargeImage(){
        return haveLargeImage;
    }
    public void setHaveLargeImage(Boolean haveLargeImage){
        this.haveLargeImage=haveLargeImage;
    }
    
    //文章内容
    private String text;
    public String getText(){
        return text;
    }
    public void setText(String text){
        this.text=text;
    }
    
  //原始微博内容
    private String originalText;
    public String getOriginalText(){
        return originalText;
    }
    public void setOriginalText(String originalText){
        this.originalText=originalText;
    }

    //微博来源
    private String source;
    public String getSource() {
		return source;
	}
    public void setSource(String source) {
		this.source = source;
	}
    
    //微博缩略图地址
    private String thumbnailPic;
    public String getThumbnailPic() {
		return thumbnailPic;
	}
    public void setThumbnailPic(String thumbnailPic) {
		this.thumbnailPic = thumbnailPic;
	}
   
  //微博大图地址
    private String largePic;
    public String getLargePic() {
		return largePic;
	}
    public void setLargePic(String largePic) {
		this.largePic = largePic;
	}
}
