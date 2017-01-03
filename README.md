# LikeIReadTabLayout
仿掌阅的TabLayout

##项目地址
https://github.com/CSnowStack/LikeIReadTabLayout

##预览
![预览](https://github.com/CSnowStack/LikeIReadTabLayout/blob/master/img/preview.gif)

## 代码很简单,只是在TabLayout的代码上改几行,指示器的宽度实现的好像不对
```java
private void updateIndicatorPosition() {
           final View selectedTitle = getChildAt(mSelectedPosition);
           int left, right;
           if (selectedTitle != null && selectedTitle.getWidth() > 0) {
               left = (int) (selectedTitle.getLeft()+selectedTitle.getWidth()*0.2f);
               right = (int) (selectedTitle.getRight()-selectedTitle.getWidth()*0.2f);

               if (mSelectionOffset > 0f && mSelectedPosition < getChildCount() - 1) {
                   // Draw the selection partway between the tabs
                   View nextTitle = getChildAt(mSelectedPosition + 1);
                   int nextLeft= (int) (nextTitle.getLeft()+nextTitle.getWidth()*0.2f);
                   int nextRight= (int) (nextTitle.getRight()-nextTitle.getWidth()*0.2f);

                   if(mSelectionOffset>0.5f){
                       left= (int) (left+(nextLeft-left)*(mSelectionOffset-0.5f)*2);
                       right=nextRight;
                   }else {
                       right= (int) (right+(nextRight-right)*(mSelectionOffset*2));
                   }
               }
           } else {
               left = right = -1;
           }

           setIndicatorPosition(left, right);
}

void animateIndicatorToPosition(final int position, int duration) {
  //...没改的就不贴上来了
  final int targetLeft = (int) (targetView.getLeft()+targetView.getWidth()*0.2);
  inal int targetRight = (int) (targetView.getRight()-targetView.getWidth()*0.2);

  animator.addUpdateListener(new ValueAnimatorCompat.AnimatorUpdateListener() {
                  @Override
                  public void onAnimationUpdate(ValueAnimatorCompat animator) {
                      final float fraction = animator.getAnimatedFraction();
                      setIndicatorPosition(
                              AnimationUtils.lerp(startLeft, targetLeft, fraction,true),
                              AnimationUtils.lerp(startRight, targetRight, fraction,false));
                  }
              });
}

static int lerp(int startValue, int endValue, float fraction,boolean isLeftPoint) {
      if((endValue>startValue&& !isLeftPoint) ||(endValue<startValue&&isLeftPoint)){
          return fraction>0.5?endValue:startValue + Math.round(fraction *2* (endValue - startValue));
      }else {
          return fraction > 0.5 ? startValue + Math.round((fraction - 0.5f) * 2 * (endValue - startValue)) : startValue;
      }
}
```
