package munch.data.location;

import java.util.List;

/**
 * Official supported city in munch-data
 * <p>
 * Created by: Fuxing
 * Date: 27/8/2018
 * Time: 7:39 PM
 * Project: munch-data
 */
public enum City {
    singapore;

    public List<String> getPoints() {
        switch (this) {
            case singapore:
                return List.of("1.26675774823251,103.60313415527344", "1.3244212231757635,103.61755371093749", "1.3896342476555246,103.65325927734375", "1.4143460858068593,103.66630554199219", "1.4294476354255539,103.67179870605467", "1.439057660807751,103.68278503417969", "1.4438626583311722,103.69583129882812", "1.4589640128389818,103.72055053710938", "1.4582775898253464,103.73771667480469", "1.4493540716333067,103.75419616699219", "1.4500404973607948,103.7603759765625", "1.4788701887242242,103.80363464355467", "1.4754381021049132,103.8269805908203", "1.4582775898253464,103.86680603027342", "1.4321933610794366,103.8922119140625", "1.4287612034988086,103.89701843261717", "1.4267019064882447,103.91555786132812", "1.4321933610794366,103.93478393554688", "1.4218968729661605,103.96018981933592", "1.4246426076343077,103.985595703125", "1.4212104387885494,104.00070190429688", "1.4397440896459617,104.02130126953125", "1.445921939876798,104.04396057128906", "1.4246426076343077,104.08721923828125", "1.3971851147344805,104.09477233886719", "1.3573711816421556,104.08103942871094", "1.290097884072079,104.12704467773438", "1.2777413679950957,104.12704467773438", "1.2537146393239096,103.98216247558594", "1.1754546449158993,103.81256103515625", "1.1301452152248344,103.73634338378906", "1.1905576261723045,103.65394592285156", "1.1960495988987414,103.56536865234375", "1.26675774823251,103.60313415527344");
            default:
                return null;
        }
    }
}