package swcnoops.server.game;

import swcnoops.server.game.Joe.JoeFile;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PatchData {
    private Map<Class<? extends JoeData>, Map<String,JoeData>> dataMap = new HashMap<>();
    public void merge(JoeFile.JoeObjects joeObjects) throws Exception {
        merge((List<JoeData>)(List<?>)joeObjects.getTournamentData());
        merge((List<JoeData>)(List<?>)joeObjects.getCampaignData());
        merge((List<JoeData>)(List<?>)joeObjects.getCampaignMissionData());
    }

    private void merge(List<JoeData> patchDatum) throws Exception {
        if (patchDatum != null && patchDatum.size() > 0) {
            JoeData joeData = patchDatum.get(0);
            Map<String, JoeData> joeDataMap = this.dataMap.get(joeData.getClass());
            if (joeDataMap == null) {
                joeDataMap = new HashMap<>();
                this.dataMap.put(joeData.getClass(), joeDataMap);
            }

            for (JoeData data : patchDatum) {
                JoeData existingData = joeDataMap.get(data.getUid());
                if (existingData == null) {
                    existingData = data.getClass().getDeclaredConstructor().newInstance();
                    joeDataMap.put(data.getUid(), existingData);
                }

                merge(existingData, data);
            }
        }
    }

    private void merge(JoeData existingData, JoeData data) throws Exception {
        Class<?> clazz = data.getClass();
        for (Field field : clazz.getFields()) {
            set(field, existingData, data);
        }
    }

    private void set(Field field, JoeData existingData, JoeData data) throws Exception {
        Object value = field.get(data);
        if (field.getType().isPrimitive()) {
            if (!isZero(value)) {
                field.set(existingData, value);
            }
        } else {
            if (value != null) {
                field.set(existingData, value);
            }
        }
    }

    private boolean isZero(Object value) {
        if (value instanceof Integer) {
            return (((Integer)value).intValue() == 0);
        } else if (value instanceof Long) {
            return (((Long)value).longValue() == 0);
        }

        throw new RuntimeException("Unsupported primitive type " + value.getClass());
    }

    public <T extends JoeData> Map<String, T> getMap(Class<T> clazz) {
        Map<String, T> map = (Map<String,T>) this.dataMap.get(clazz);
        return map;
    }
}
