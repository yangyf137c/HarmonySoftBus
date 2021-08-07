import com.hoperun.control.slice.DocAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;


public class DocAbility extends Ability {

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setMainRoute(DocAbilitySlice.class.getName());
        //虽然一个 Page 可以包含多个 AbilitySlice，但是 Page 进入前台时界面默认只展示一个 AbilitySlice。
        // 默认展示的 AbilitySlice 是通过setMainRoute()方法来指定的。
    }

}
