package fi.dy.masa.servux.schematic.container;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;

import javax.annotation.Nullable;
import java.util.List;

public class LitematicaBlockStatePaletteLinear implements ILitematicaBlockStatePalette
{
    private final BlockState[] states;
    private final ILitematicaBlockStatePaletteResizer resizeHandler;
    private final int bits;
    private int currentSize;

    public LitematicaBlockStatePaletteLinear(int bitsIn, ILitematicaBlockStatePaletteResizer resizeHandler)
    {
        this.states = new BlockState[1 << bitsIn];
        this.bits = bitsIn;
        this.resizeHandler = resizeHandler;
    }

    @Override
    public int idFor(BlockState state)
    {
        for (int i = 0; i < this.currentSize; ++i)
        {
            if (this.states[i] == state)
            {
                return i;
            }
        }

        final int size = this.currentSize;

        if (size < this.states.length)
        {
            this.states[size] = state;
            ++this.currentSize;
            return size;
        }
        else
        {
            return this.resizeHandler.onResize(this.bits + 1, state);
        }
    }

    @Override
    @Nullable
    public BlockState getBlockState(int indexKey)
    {
        return indexKey >= 0 && indexKey < this.currentSize ? this.states[indexKey] : null;
    }

    @Override
    public int getPaletteSize()
    {
        return this.currentSize;
    }

    private void requestNewId(BlockState state)
    {
        final int size = this.currentSize;

        if (size < this.states.length)
        {
            this.states[size] = state;
            ++this.currentSize;
        }
        else
        {
            int newId = this.resizeHandler.onResize(this.bits + 1, LitematicaBlockStateContainer.AIR_BLOCK_STATE);

            if (newId <= size)
            {
                this.states[size] = state;
                ++this.currentSize;
            }
        }
    }

    @Override
    public void readFromNBT(NbtList tagList)
    {
        RegistryEntryLookup<Block> lookup = Registries.BLOCK.getReadOnlyWrapper();
        final int size = tagList.size();

        for (int i = 0; i < size; ++i)
        {
            NbtCompound tag = tagList.getCompound(i);
            BlockState state = NbtHelper.toBlockState(lookup, tag);

            if (i > 0 || state != LitematicaBlockStateContainer.AIR_BLOCK_STATE)
            {
                this.requestNewId(state);
            }
        }
    }

    @Override
    public NbtList writeToNBT()
    {
        NbtList tagList = new NbtList();

        for (int id = 0; id < this.currentSize; ++id)
        {
            BlockState state = this.states[id];

            if (state == null)
            {
                state = LitematicaBlockStateContainer.AIR_BLOCK_STATE;
            }

            NbtCompound tag = NbtHelper.fromBlockState(state);
            tagList.add(tag);
        }

        return tagList;
    }

    @Override
    public boolean setMapping(List<BlockState> list)
    {
        final int size = list.size();

        if (size <= this.states.length)
        {
            for (int id = 0; id < size; ++id)
            {
                this.states[id] = list.get(id);
            }

            this.currentSize = size;

            return true;
        }

        return false;
    }
}
