package appeng.tile.misc;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.EnumSet;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.networking.GridFlags;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.util.AECableType;
import appeng.me.GridAccessException;
import appeng.tile.events.AETileEventHandler;
import appeng.tile.events.TileEventType;
import appeng.tile.grid.AENetworkTile;

public class TileQuartzGrowthAccelerator extends AENetworkTile implements IPowerChannelState
{

	public boolean hasPower = false;

	@MENetworkEventSubscribe
	public void onPower(MENetworkPowerStatusChange ch)
	{
		markForUpdate();
	}

	@Override
	public AECableType getCableConnectionType(ForgeDirection dir)
	{
		return AECableType.COVERED;
	}

	private class TileChargerHandler extends AETileEventHandler
	{

		public TileChargerHandler() {
			super( TileEventType.NETWORK );
		}

		@Override
		public boolean readFromStream(ByteBuf data) throws IOException
		{
			boolean hadPower = hasPower;
			hasPower = data.readBoolean();
			return hasPower != hadPower;
		}

		@Override
		public void writeToStream(ByteBuf data) throws IOException
		{
			try
			{
				data.writeBoolean( gridProxy.getEnergy().isNetworkPowered() );
			}
			catch (GridAccessException e)
			{
				data.writeBoolean( false );
			}
		}

	};

	public TileQuartzGrowthAccelerator() {
		gridProxy.setValidSides( EnumSet.noneOf( ForgeDirection.class ) );
		gridProxy.setFlags( GridFlags.CANNOT_CARRY );
		gridProxy.setIdlePowerUsage( 2 );
		addNewHandler( new TileChargerHandler() );
	}

	@Override
	public void setOrientation(ForgeDirection inForward, ForgeDirection inUp)
	{
		super.setOrientation( inForward, inUp );
		gridProxy.setValidSides( EnumSet.of( getUp(), getUp().getOpposite() ) );
	}

	@Override
	public boolean isPowered()
	{
		return hasPower;
	}

	@Override
	public boolean isActive()
	{
		return hasPower;
	}

}