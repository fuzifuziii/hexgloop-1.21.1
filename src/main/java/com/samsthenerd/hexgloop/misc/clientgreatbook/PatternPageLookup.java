package com.samsthenerd.hexgloop.misc.clientgreatbook;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.samsthenerd.hexgloop.mixins.booktweaks.MixinClientBookPageAccessor;
import com.samsthenerd.hexgloop.mixins.booktweaks.MixinClientGetPageTemplate;
import com.samsthenerd.hexgloop.mixins.booktweaks.MixinClientGetTemplateComponents;
import com.samsthenerd.hexgloop.mixins.booktweaks.MixinClientGetTemplateText;
import com.samsthenerd.hexgloop.mixins.booktweaks.MixinClientYoinkPPLangKey;
import com.samsthenerd.hexgloop.utils.patternmatching.PatternMatching;

import at.petrak.hexcasting.api.HexAPI;
import at.petrak.hexcasting.api.casting.math.HexPattern;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.BookPage;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.template.TemplateComponent;
import vazkii.patchouli.client.book.text.BookTextParser;
import vazkii.patchouli.client.book.text.Span;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;
public class PatternPageLookup {
    // cache them up here
    private static final Map<ResourceLocation, BookPage> foundPages = new HashMap<>();

    public static BookPage findPage(ResourceLocation id) {
        if(foundPages.containsKey(id)) {
            return foundPages.get(id);
        }
        // fine if it's null
        BookPage foundPage = searchForPage(id);
        foundPages.put(id, foundPage);
        return foundPage;
    }

    public static BookPage findPage(HexPattern pattern){
        ResourceLocation id = PatternMatching.getIdentifier(pattern);
        // HexGloop.logPrint("got id: " + id);
        if(id != null){
            BookPage foundPage = findPage(id);
            // HexGloop.logPrint("got page: " + foundPage);
            return foundPage;
        }
        return null;
    }

    private static Component patchiFormat(Component text){
        Component baseText = text;
        if(text.getContents() instanceof PlainTextContents.LiteralContents lc) {
			baseText = Component.literal(I18n.get(lc.text()));
		}
        Book hexbook = BookRegistry.INSTANCE.books.get(HexAPI.modLoc("thehexbook"));
        GuiBook gui = hexbook.getContents().getCurrentGui();
        BookTextParser parser = new BookTextParser(gui, hexbook, 0, 0, GuiBook.PAGE_WIDTH, GuiBook.TEXT_LINE_HEIGHT, Style.EMPTY);
        MutableComponent styledText = Component.empty();
        for(Span span : parser.parse(baseText)){
            styledText.append(Component.literal(span.text).setStyle(span.style));
        }
        return styledText;
    }

    public static Tuple<Component, Component> getDescription(ResourceLocation id) {
        BookPage page = findPage(id);
        if(page instanceof MixinClientGetPageTemplate pageTemplate && pageTemplate.getTemplate() instanceof MixinClientGetTemplateComponents bookTemplate) {
            int foundTexts = 0;
            Component argText = Component.empty();
            for(TemplateComponent component : bookTemplate.getComponents()){
                if(component instanceof MixinClientGetTemplateText textComponent){
                    if(foundTexts == 0){
                        // it's the input/output, which could still be useful !
                        argText = patchiFormat(textComponent.getActualText());
                    } else {
                        return new Tuple<>(argText, patchiFormat(textComponent.getActualText()));
                    }
                    foundTexts++;
                }
            }
        }
        return new Tuple<>(Component.literal("No description found for " + id.toString()), Component.empty());
    }

    public static Tuple<Component, Component> getDescription(HexPattern pattern){
        ResourceLocation id = PatternMatching.getIdentifier(pattern);
        if(id != null){
            return getDescription(id);
        }
        return new Tuple<>(Component.empty(), Component.empty());
    }

    private static Map<String, String> ID_REPLACEMENTS = new HashMap<>();
    static{
        ID_REPLACEMENTS.put("hexcasting:const/vec/px", "hexcasting:const/vec/x");        
        ID_REPLACEMENTS.put("hexcasting:const/vec/nx", "hexcasting:const/vec/x");        
        ID_REPLACEMENTS.put("hexcasting:const/vec/py", "hexcasting:const/vec/y");        
        ID_REPLACEMENTS.put("hexcasting:const/vec/ny", "hexcasting:const/vec/y");        
        ID_REPLACEMENTS.put("hexcasting:const/vec/pz", "hexcasting:const/vec/z");        
        ID_REPLACEMENTS.put("hexcasting:const/vec/nz", "hexcasting:const/vec/z");        
    }

    @Nullable
    private static BookPage searchForPage(ResourceLocation id){
        // HexGloop.logPrint("need to search for page for " + id.toString());
        if(ID_REPLACEMENTS.containsKey(id.toString())){
            id = ResourceLocation.parse(ID_REPLACEMENTS.get(id.toString()));
        }
        Book hexbook = BookRegistry.INSTANCE.books.get(HexAPI.modLoc("thehexbook"));
        for(BookEntry entry : hexbook.getContents().entries.values()){
            // HexGloop.logPrint("checking entry: " + entry.getName());
            for(BookPage page : entry.getPages()){
                String anchor = ((MixinClientBookPageAccessor)(Object)page).getAnchor();
                if(id.toString().equals(anchor)){
                    return page;
                }
                if(page instanceof MixinClientGetPageTemplate pageTemplate 
                && pageTemplate.getTemplate() instanceof MixinClientGetTemplateComponents bookTemplate 
                && bookTemplate.getProcessor() instanceof MixinClientYoinkPPLangKey patternProcessor) {
                    String transKey = patternProcessor.getTranslationKey();
                    String opId = transKey.replace("hexcasting.spell.book.", "").replace("hexcasting.spell.", "");
                    if(id.toString().equals(opId)){
                        return page;
                    }
                }
            }
        }
        return null;
    }
}
