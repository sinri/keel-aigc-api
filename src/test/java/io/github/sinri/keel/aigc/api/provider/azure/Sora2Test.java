package io.github.sinri.keel.aigc.api.provider.azure;

import io.github.sinri.keel.base.configuration.ConfigElement;
import io.github.sinri.keel.tesuto.KeelInstantRunner;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

public class Sora2Test extends KeelInstantRunner {
    @Override
    protected Future<Void> run() throws Exception {
        /*
        curl -X POST "https://eighthtowerineastus2.openai.azure.com/openai/v1/videos" \
  -H "Content-Type: application/json" \
  -H "Api-key: $AZURE_API_KEY" \
  -d '{
     "model": "sora-2",
     "prompt" : "A video of a cat",
     "size" : "720x1280",
     "seconds" : "4"
    }'
         */

        String prompt = """
                        Style: 都市轻奢风格，带戏剧冲突感，画面精致有张力，突出人物情绪对比与身份差异
                        Cinematography：
                        
                        - Camera：中景展现餐桌互动，特写捕捉面部表情与手部动作，用镜头语言强化两人气场反差
                        - Lens：50mm镜头，清晰呈现人物神态细节与餐厅精致环境
                        - Lighting：餐厅暖黄主灯聚焦餐桌，侧光勾勒贵妇面部轮廓，凸显肤质通透感
                        - Mood：表面优雅下的暗地较量，自信从容与局促尴尬的鲜明碰撞
                        Shots:
                        - Shot 1
                        - Duration：0:00 - 0:04
                        - Scene：高级餐厅内，40岁贵妇身着香槟色缎面连衣裙，颈间珍珠项链随动作轻晃，坐姿优雅；对面闺蜜穿亮片吊带裙，妆容浓艳，正端着红酒杯冷笑开口
                        - Shot 2
                        - Duration：0:04 - 0:08
                        - Scene：贵妇指尖涂着豆沙色甲油，从鳄鱼纹手包中取出一物，语气平淡回应；闺蜜戴着夸张耳环的头往前探，伸手一把夺过物品翻来覆去查看
                        - Shot 3
                        - Duration：0:08 - 0:12
                        - Scene：闺蜜皱眉咋舌，假睫毛随着夸张表情颤动；贵妇夺回物品，眼神扫过闺蜜鼻翼卡粉的细纹，抬手轻抚自己细腻脸颊，肌肤在灯光下泛着自然光泽
                        - Shot 4
                        - Duration：0:12 - 0:15
                        - Scene：闺蜜脸色涨红，亮片裙肩带滑落也顾不上拉，高声叫服务员；服务员西装笔挺上前，面露难色回应；闺蜜手捏着桌布指尖发白，贵妇唇角微扬轻笑，镜头特写那件物品
                        高级餐厅里，水晶灯折射出细碎光芒。40岁贵妇身着香槟色缎面连衣裙，领口微敞露出精致锁骨，颈间珍珠项链随着细微动作轻轻晃动，坐姿如教科书般优雅，举手投足带着久经场合的从容。对面的闺蜜穿着一身紧绷的亮片吊带裙，浓黑的眼线晕了些许，正端着红酒杯，唇角撇出一抹冷笑：“你最近皮肤好了不少啊，偷偷去做医美了？”
                        
                        贵妇眼皮都没抬，涂着豆沙色甲油的指尖从鳄鱼纹手包里拈出一样东西，放在桌布上推了推，语气平淡得像在说天气：“天生好皮，这个道理你还不懂？”闺蜜戴着夸张水钻耳环的头猛地往前探，涂着大红甲油的手一把夺过物品，翻来覆去看了看，突然拔高声音：“这么个小东西要几百？抢钱啊！”
                        
                        贵妇慢悠悠夺回物品，眼神淡淡扫过闺蜜鼻翼卡出的粉纹，抬手轻轻拂过自己的脸颊，指尖划过处肌肤细腻得像剥了壳的鸡蛋：“你看，你脸上的粉都卡成地图了，我的可是完全融进皮肤里。”闺蜜的脸瞬间涨成猪肝色，假睫毛随着急促的呼吸簌簌颤动，猛地转头朝服务员招手：“给我也来一瓶这个！现在就要！”
                        
                        穿西装的服务员快步上前，看到桌上的物品后愣了愣，礼貌地欠身：“女士，这不是我们菜单上的餐品哦。”闺蜜的手猛地攥紧桌布，亮片裙的肩带滑到胳膊肘也没察觉，整个人僵在座位上。贵妇终于忍不住，低低笑出了声，镜头缓缓推近，定格在那件物品上。
                        
                        视频时长:12秒
                        画面比例：9:16
                        以下是任务考核点，强制要求：
                        
                        - 视频要求：真实、有情绪、有动态、有冲突
                        - 语言要求：zh - CN
                        - 拍摄设备：iphone 17 Pro
                        - 视频参数：分辨率4K、比例9:16
                        - 声音要求：同期声，声音与口型一致，搭配餐厅环境音效与烘托氛围的背景音乐
                        - duration：12s
                        - model：portrait - hd
                        """;
        String apiKey = ConfigElement.root().readProperty("provider.azure.openai.sora-2.apiKey");
        return WebClient.create(getKeel())
                        .postAbs("https://eighthtowerineastus2.openai.azure.com/openai/v1/videos")
                        .bearerTokenAuthentication("")
                        .sendJsonObject(new JsonObject()
                                .put("model", "sora-2")
                                .put("prompt", prompt)
                                .put("size", "720x1280")
                                .put("seconds", "12")
                        )
                        .compose(bufferHttpResponse -> {
                            getLogger().info("Response received: " + bufferHttpResponse.statusCode());
                            var resp = bufferHttpResponse.bodyAsString();
                            getLogger().info("Response: \n" + resp);
                            return Future.succeededFuture();
                        });
    }
}
