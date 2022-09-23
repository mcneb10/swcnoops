package swcnoops.server.game;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameConstants {
    public Integer crystals_speed_up_coefficient;
    public Integer crystals_speed_up_exponent;
    public Integer crystals_speed_up_prestige_coefficient;
    public Integer crystals_speed_up_prestige_exponent;
    public Integer crystals_speed_up_building_lever_percentage;
    public Integer coef_exp_accuracy;
    public Float contract_refund_percentage_buildings;
    public Float contract_refund_percentage_troops;
    public Integer upgrade_all_walls_coefficient;
    public Integer upgrade_all_wall_exponent;
    public Integer upgrade_all_walls_convenience_tax;
    public Integer credits_coefficient;
    public Integer credits_exponent;
    public Float pvp_battle_one_star_victory;
    public Float pvp_battle_two_star_victory;
    public Float pvp_battle_three_star_victory;
    public Integer new_player_protection;
    public Integer pvp_damage_protection_1star;
    public Integer pvp_damage_protection_2star;
    public Integer pvp_damage_protection_3star;

    public String pvp_search_cost_by_hq_level;
    public Integer pvp_match_countdown;
    public Integer pvp_match_duration;
    public Integer squad_create_cost;

    public static GameConstants createFromBaseJson(List<Map<String, String>> gameConstants) throws Exception {
        GameConstants constants = new GameConstants();
        constants.setConstants(gameConstants);
        return constants;
    }

    private void setConstants(List<Map<String, String>> gameConstants) throws Exception {
        Map<String, Map<String, String>> gameConstantMap = createMap(gameConstants);

        Class<?> clazz = this.getClass();
        for (Field field : clazz.getFields()) {
            String fieldName = field.getName();
            if (gameConstantMap.containsKey(fieldName)) {
                set(field, this, gameConstantMap.get(fieldName).get("value"));
            }
        }
    }

    private void set(Field field, GameConstants config, String property) throws Exception {
        Object value = convertToObject(property, field.getType());
        field.set(config, value);
    }

    private Object convertToObject(String property, Class<?> type) {
        if (type == Integer.class) {
            return Integer.valueOf(property);
        } else if (type == String.class) {
            return property;
        } else if (type == Float.class) {
            return Float.valueOf(property);
        } else if (type == Double.class) {
            return Double.valueOf(property);
        } else if (type == Long.class) {
            return Long.valueOf(property);
        } else if (type == Boolean.class) {
            return Boolean.valueOf(property);
        }

        return null;
    }

    private Map<String, Map<String, String>> createMap(List<Map<String, String>> gameConstants) {
        Map<String, Map<String, String>> map = new HashMap<>();

        for (Map<String, String> constant : gameConstants) {
            map.put(constant.get("uid"), constant);
        }
        return map;
    }
}
