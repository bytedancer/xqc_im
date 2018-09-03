package com.bonade.xxp.xqc_android_im.ui.widget.message;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.bonade.xxp.xqc_android_im.DB.entity.MessageEntity;
import com.bonade.xxp.xqc_android_im.DB.entity.UserEntity;
import com.bonade.xxp.xqc_android_im.R;
import com.bonade.xxp.xqc_android_im.imservice.entity.TextMessage;
import com.bonade.xxp.xqc_android_im.ui.helper.Emoparser;

import java.util.regex.Pattern;

public class TextRenderView extends BaseMsgRenderView {

    private TextView msgContentView;

//    public static TextRenderView inflater(Context context, boolean isMine) {
//        int resource = isMine ? R.layout.item_mine_text_message : R.layout.item_other_text_message;
//
//        TextRenderView textRenderView = (TextRenderView) LayoutInflater.from(context).inflate(resource, null);
//        textRenderView.setMine(isMine);
//        return textRenderView;
//
//    }

    public TextRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        msgContentView = findViewById(R.id.tv_message_content);
    }

//    @Override
//    protected void onFinishInflate() {
//        super.onFinishInflate();
//        msgContentView = findViewById(R.id.tv_message_content);
//    }

    /**
     * 控件赋值
     * @param messageEntity
     * @param userEntity
     */
    @Override
    public void render(@NonNull MessageEntity messageEntity, @NonNull UserEntity userEntity, Context context) {
        super.render(messageEntity, userEntity, context);
        TextMessage textMessage = (TextMessage) messageEntity;
        // 按钮的长按也是上层设定的
        // url 路径可以设定 跳转哦哦
        String content = textMessage.getContent();
        // 所以上层还是处理好之后再给我 Emoparser 处理之后的
        msgContentView.setText(Emoparser.getInstance(getContext()).emoCharsequence(content));
        extractUrl2Link(msgContentView);
    }

    private static final String SCHEMA ="";
    private static final String PARAM_UID ="";
    private String urlRegex = "((?:(http|https|Http|Https|rtsp|Rtsp):\\/\\/(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})?\\@)?)?((?:(?:[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}\\.)+(?:(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])|(?:biz|b[abdefghijmnorstvwyz])|(?:cat|com|coop|c[acdfghiklmnoruvxyz])|d[ejkmoz]|(?:edu|e[cegrstu])|f[ijkmor]|(?:gov|g[abdefghilmnpqrstuwy])|h[kmnrtu]|(?:info|int|i[delmnoqrst])|(?:jobs|j[emop])|k[eghimnrwyz]|l[abcikrstuvy]|(?:mil|mobi|museum|m[acdghklmnopqrstuvwxyz])|(?:name|net|n[acefgilopruz])|(?:org|om)|(?:pro|p[aefghklmnrstwy])|qa|r[eouw]|s[abcdeghijklmnortuvyz]|(?:tel|travel|t[cdfghjklmnoprtvwz])|u[agkmsyz]|v[aceginu]|w[fs]|y[etu]|z[amw]))|(?:(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])))(?:\\:\\d{1,5})?)(\\/(?:(?:[a-zA-Z0-9\\;\\/\\?\\:\\@\\&\\=\\#\\~\\-\\.\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?(?:\\b|$)";
    private void extractUrl2Link(TextView textView) {
        Pattern wikiWordMatcher = Pattern.compile(urlRegex);
        String mentionsScheme = String.format("%s/?%s=",SCHEMA, PARAM_UID);
        Linkify.addLinks(textView, wikiWordMatcher, mentionsScheme);
    }

    @Override
    public void msgFailure(MessageEntity entity) {
        super.msgFailure(entity);
    }

    public TextView getMsgContentView() {
        return msgContentView;
    }

    public void setMsgContentView(TextView msgContentView) {
        this.msgContentView = msgContentView;
    }

    public boolean isMine() {
        return isMine;
    }

    public void setMine(boolean isMine) {
        this.isMine = isMine;
    }
}
