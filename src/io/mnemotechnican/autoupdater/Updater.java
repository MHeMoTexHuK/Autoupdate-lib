package io.mnemotechnician.autoupdater;

import java.io.*;
import arc.struct.*;
import arc.util.*;
import arc.files.*;
import arc.util.io.*;
import mindustry.*;
import mindustry.mod.*;

public class Updater {
	
	//todo: support for non-standard branches?
	public static final String url = "https://github.com/";
	public static final String tokenVersion = "VERSION", tokenRepo = "REPO";
	//temporary builder
	public static final StringBuilder check = new StringBuilder();
	
	//used for Relfect.invoke
	static final Object[] args = {null, false};
	static final Class[] args2 = {String.class, boolean.class};
	
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
		
		ObjectMap<String, Object> info = readInfo(meta);
		float currentVersion = -1;
		String currentRepo = null;
		try {
			currentVersion = Float.valueOf(info.get(tokenVersion, -1));
			currentRepo = String.valueOf(info.get(tokenRepo, null));
		} catch (NumberFormatException e) {
			Log.err("Incorrect token value type!");
			Log.err(e.toString()); //no need to print stack trace
			return;
		}
		if (!validate(currentVersion, currentRepo)) return;
		
		//FUCKKKKKKKKKKKKKKKKKKKKING LAMBDAS
		final float vfinal = currentVersion;
		final String rfinal = currentRepo;
		
		Fi temp = Fi.tempFile("update-check");
		//try to find the file in the root
		Http.get(url + rfinal + "/blob/master/mod.hjson")
		.error(e -> {
			//try to find it in the assets folder
			Http.get(url + rfinal + "/blob/master/assets/mod.hjson")
			.error(ee -> {
				Log.err("Couldn't fetch the remote metainfo file!");
				Log.err(ee);
			})
			.submit(r -> {
				temp.writeBytes(r.getResult());
				tryUpdate(temp, vfinal, mod);
			});
		})
		.submit(r -> {
			temp.writeBytes(r.getResult());
			tryUpdate(temp, vfinal, mod);
		});
	}
	
	protected static void tryUpdate(Fi metainfo, float currentVersion, Mods.LoadedMod mod) {
		ObjectMap<String, Object> info = readInfo(metainfo);
		float newVersion = -1;
		String newRepo = null; //migration support
		try {
			newVersion = (float) info.get(tokenVersion, -1);
			newRepo = (String) info.get(tokenRepo, null);
		} catch (ClassCastException e) {
			Log.err("Incorrect token value type!");
			Log.err(e.toString()); //no need to print stack trace
			return;
		}
		if (!validate(newVersion, newRepo)) return;
		
		final String nrfinal = newRepo; //I FUCKING CAN'T
		Vars.ui.showCustomConfirm(
			"Update available",
			"New version of " + mod.name + " available!",
			"[green]Update",
			"[red]Not now",
			
			() -> {
				args[0] = nrfinal;
				Reflect.invoke(Vars.ui.mods, "githubImportMod", args, args2);
			},
			
			() -> {}
		);
	}
	
	/** Returns whether the hhh is valid. Prints to console if it isn't. */
	protected static boolean validate(float currentVersion, String currentRepo) {
		if (currentVersion == -1 || currentRepo == null) {
			Log.err("You must specify both current version and repo in your mod.hjson file!");
			Log.err("Specify \"#!VERSION number;\" and \"#!REPO user/repository\" in your mod.hjson file and try again!");
			return false;
		} else if (currentRepo.indexOf("/") == -1 || currentRepo.lastIndexOf("/") != currentRepo.indexOf("/")) {
			Log.err("Malformed repository path! Repo must contain only 1 slash character!");
			return false;
		}
		return true;
	}
	
	public static Fi getMeta(Fi root) {
		return root.child("mod.hjson").exists() ? root.child("mod.hjson")
			: root.child("mod.json").exists() ? root.child("mod.json") : null;
	}
	
	/** Reads the providen meta-info file and, unless an error occurs, returns an ObjectMap containing all control tokens and their respective values */
	protected static ObjectMap<String, Object> readInfo(Fi meta) {
		ObjectMap<String, Object> map = new ObjectMap(8);
		InputStream read = null;
		try {
			read = meta.read();
			check.setLength(0);
			int b = 0;
			
			global:
			while (b != -1) {
				//skip to the next #!. ##! will be ignored.
				while ((b = read.read()) != '#' || b != '!'){
					if (b == -1) break global;
					if (b == '\\') read.read(); //skip next character
				}
				
				//read token name
				while ((b = read.read()) != ' ') {
					if (b == -1) break global;
					check.append((char) b);
				}
				String key = check.toString();
				check.setLength(0);
				
				//read token value. can be a string or an integer/float
				b = read.read();
				if (b == '"') { //it's a string, read till the next " symbol
					while ((b = read.read()) != '"') {
						if (b == -1) break global;
						check.append((char) b);
					}
					String value = check.toString();
					map.put(key, value);
				} else if (Character.isDigit((char) b)) { //it's a number, read as long as possible
					boolean hasPoint = false;
					while (Character.isDigit(b = read.read()) || (b == '.' && !hasPoint && (hasPoint = true))) {
						if (b == -1) break global;
						check.append((byte) b);
					}
					
					try {
						if (hasPoint) {
							float value = Float.valueOf(check.toString());
							map.put(key, value);
							
						} else {
							int value = Integer.valueOf(check.toString());
							map.put(key, value);
						}
					} catch (Throwable e) {
						continue global; //somehow they managed to break this failsafe system, ignore this token
					}
				}
			}
			
			return map;
		} catch (Exception e) {
			Log.err("Exception occurred while reading mod info: " + meta.name(), e);
			return null;
		} finally {
			try {
				if (read != null) read.close();
			} catch (Throwable e) {
				Log.info("fuck checked exceptions");
				throw new RuntimeException(e);
			}
		}
	}
	
}