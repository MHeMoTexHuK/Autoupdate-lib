package io.mnemotechnician.autoupdater;

import java.io.*;
import arc.util.*;
import arc.files.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.mod.*;

public class Updater {
	
	public static final String url = "https://github.com/", urlFile= "/blob/master/", urlDownload = "/archive/master.zip";
	public static final String prefixVersion = "VERSION", prefixRepo = "REPO";
	//temporary builder
	public static final StringBuilder check = new StringBuilder();
	
	protected static String repo;
	protected static int version;
	
	//used for Relfect.invoke
	static Object[] args = {null, (Boolean) false};
	static Class[] args2 = {String.class, Boolean.class};
	
	public static void checkUpdates(Mod originMod) {
		var mod = Vars.mods.getMod(originMod.getClass());
		var file = mod.file;
		
		Fi meta = null;
		if (file.isDirectory()) {
			meta = getMeta(file);
		} else if (file == null) {
			Log.err("Failed to check updates for: " + mod.name);
			return;
		} else {
			meta = getMeta(new ZipFi(file));
		}
		
		if (meta == null) {
			Log.err("Unable to locate mod info file for: " + mod.name);
			return;
		}
		
		if (!checkInfo(meta)) {
			return;
		}
		int currentVersion = version;
		String currentRepo = repo;
		
		//todo: retry upon a non-fatal error?
		Http.get(url + currentRepo + urlFile + meta.name(), response -> {
			Fi newMeta = Fi.tempFile("meta"); //unfortunately.
			newMeta.writeBytes(response.getResult());
			
			if (!checkInfo(newMeta)) {
				return;
			}
			
			if (currentVersion < version) {
				Vars.ui.showCustomConfirm(
					"Update available",
					"New version of " + mod.name + " available!",
					"[green]Update",
					"[red]Not now",
					
					() -> {
						args[0] = repo;
						Reflect.invoke(Vars.ui.mods, "githubImportMod", args, args2);
					}
					
					() -> {}
				);
			}
		});
	}
	
	public static Fi getMeta(Fi root) {
		return root.child("mod.hjson").exists() ? root.child("mod.hjson")
			: root.child("mod.json").exists() ? root.child("mod.json") : null;
	}
	
	/** Reads the providen meta-info file and, unless an error occurs, outputs the version & repo to the respective fields of this class */
	protected static boolean checkInfo(Fi meta) {
		boolean rfound = false, vfound = false;
		Reads reads = null;
		try {
			reads = meta.reads();
			byte b;
			
			global:
			while (!(rfound && vfound)) {
				if (((DataInputStream) reads.input).available() < 6) { //6 is the min num of characters for any token (#!A B;)
					return false;
				}
				
				//skip to the next #! mark. ##! will be ignored, that's intended.
				while (reads.b() != '#' || reads.b() != '!');
				
				//read control token
				check.setLength(0);
				while ((b = reads.b()) != ' ') check.append((char) b);
				
				String c = check.toString();
				if (c.equals(prefixVersion)) {
					check.setLength(0);
					while ((b = reads.b()) != ';' && b != '\n') {
						if (!Character.isDigit((char) b)) {
							Log.warn("Version must be an integer and can only contain digits, skipping");
							continue global;
						}
						check.append((char) b);
					}
					//internal version only contains digits, thus no exceptions will be thrown... i hope.
					version = Integer.valueOf(check.toString());
					vfound = true;
				} else if (c.equals(prefixRepo)) {
					check.setLength(0);
					while ((b = reads.b()) != ';' && b != '\n') check.append((char) b);
					
					if (check.indexOf("/") < 1) {
						Log.warn("Repo must be in format of AUTHOR/REPOSITY_NAME, skipping");
						continue global;
					}
					
					repo = check.toString();
					rfound = true;
				} else {
					Log.warn("Unknown control token: #!" + c);
					continue;
				}
				
			}
			
			return true;
		} catch (EOFException e) {
			Log.err("No repo/version specified in " + meta.name() + " (unexpected EOF)");
			return false;
		} catch (Exception e) {
			Log.err("Exception occurred while reading mod info: " + meta.name(), e);
			return false;
		} finally {
			if (reads != null) reads.close();
		}
	}
	
}