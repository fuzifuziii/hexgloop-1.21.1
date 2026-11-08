package com.samsthenerd.hexgloop.misc;


import java.util.List;

import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import com.samsthenerd.hexgloop.misc.clientgreatbook.GreatBook;

import at.petrak.hexcasting.api.casting.ActionRegistryEntry;
import at.petrak.hexcasting.api.mod.HexTags;
import at.petrak.hexcasting.xplat.IXplatAbstractions;
import net.minecraft.resources.ResourceKey;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.BookPage;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.item.ItemModBook;

public class GetPatchouliPatterns {
    @Nullable
    public static Book getHexBookInHand(){
        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        Book book = ItemModBook.getBook(stack);
        if(book != null && book.id.toString().equals("hexcasting:thehexbook")){
            return book;
        } else {
            return null;
        }
    }
    @NotNull
    public static Tuple<HexPattern, HexPattern> getPatternsFromEntry(BookEntry entry, int spread){
        List<BookPage> pages = entry.getPages();
        int leftNum = spread * 2;
        int rightNum = (spread * 2) + 1;

        BookPage leftPage = leftNum < pages.size() ? pages.get(leftNum) : null;
        BookPage rightPage = rightNum < pages.size() ? pages.get(rightNum) : null;

        Tuple<HexPattern, HexPattern> patterns = new Tuple<HexPattern, HexPattern>(
            getPatternFromPage(leftPage), getPatternFromPage(rightPage));

        return patterns;
    }
    @Nullable
    public static HexPattern getPatternFromPage(@Nullable BookPage page){
        if(page == null || page.sourceObject == null) return null;
        if(page.sourceObject.has("type") && page.sourceObject.has("op_id")){
            String type = page.sourceObject.get("type").getAsString();
            ResourceLocation opId = ResourceLocation.parse(page.sourceObject.get("op_id").getAsString());
            if(type.equals("hexcasting:pattern")){
                var reg = IXplatAbstractions.INSTANCE.getActionRegistry();
                var holder = reg.getHolder(ResourceKey.create(reg.key(), opId));
                if(holder.isEmpty()) return null;
                if(holder.get().is(HexTags.Actions.PER_WORLD_PATTERN)){
                    return GreatBook.INSTANCE.getPattern(opId);
                }
                return holder.get().value().prototype();
            }
        }
        return null;
    }
}
