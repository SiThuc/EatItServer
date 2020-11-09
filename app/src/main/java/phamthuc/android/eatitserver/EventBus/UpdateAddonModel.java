package phamthuc.android.eatitserver.EventBus;

import java.util.List;

import phamthuc.android.eatitserver.Model.AddonModel;

public class UpdateAddonModel {
    private List<AddonModel> addonModel;

    public UpdateAddonModel() {
    }

    public List<AddonModel> getAddonModel() {
        return addonModel;
    }

    public void setAddonModelList(List<AddonModel> addonModel) {
        this.addonModel = addonModel;
    }
}
