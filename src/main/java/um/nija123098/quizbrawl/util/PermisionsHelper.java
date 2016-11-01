package um.nija123098.quizbrawl.util;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.Permissions;

import java.util.Collections;
import java.util.EnumSet;

/**
 * Made by Dev on 10/12/2016
 */
public class PermisionsHelper {
    private EnumSet<Permissions> allow;
    private EnumSet<Permissions> deny;
    private IUser user;
    private IChannel channel;
    public PermisionsHelper(IUser user, IChannel channel) {
        this.user = user;
        this.channel = channel;
        if (this.channel.getUserOverrides().containsKey(user.getID())){
            this.allow = this.channel.getUserOverrides().get(user.getID()).allow();
            this.deny = this.channel.getUserOverrides().get(user.getID()).deny();
        }else{
            this.allow = EnumSet.noneOf(Permissions.class);
            this.deny = EnumSet.noneOf(Permissions.class);
        }
    }
    public PermisionsHelper addAllow(Permissions permission){
        this.deny.remove(permission);
        this.allow.add(permission);
        return this;
    }
    public PermisionsHelper addAllow(Permissions...permissionses){
        for (int i = 0; i < permissionses.length; i++) {
            this.deny.remove(permissionses[i]);
        }
        Collections.addAll(this.allow, permissionses);
        return this;
    }
    public PermisionsHelper addDeny(Permissions permission){
        this.allow.remove(permission);
        this.deny.add(permission);
        return this;
    }
    public PermisionsHelper addDeny(Permissions...permissionses){
        for (int i = 0; i < permissionses.length; i++) {
            this.allow.remove(permissionses[i]);
        }
        Collections.addAll(this.deny, permissionses);
        return this;
    }
    public void enact(){
        RequestHandler.request(() -> this.channel.overrideUserPermissions(this.user, this.allow, this.deny));
    }
}
