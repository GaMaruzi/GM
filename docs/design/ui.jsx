// ui.jsx — Material 3 building blocks
// Exports to window: M3Button, M3Fab, TopBar, StateLayer, Divider, Chip, Menu, Sheet

function StateLayer({ color, radius = 12, opacity = 0.1 }) {
  // hover/press tint — used inside relatively-positioned tappables
  return null; // simplified: press handled inline via onPointer styling
}

function M3Button({ kind = 'filled', icon, children, onClick, C, full, disabled, size = 'md' }) {
  const h = size === 'lg' ? 56 : 40;
  const styles = {
    filled:   { background: disabled ? C.onSurface + '1f' : C.primary, color: disabled ? C.onSurface + '61' : C.onPrimary, border: 'none' },
    tonal:    { background: C.secondaryContainer, color: C.onSecondaryContainer, border: 'none' },
    outlined: { background: 'transparent', color: C.primary, border: `1px solid ${C.outlineVariant}` },
    text:     { background: 'transparent', color: C.primary, border: 'none' },
    elevated: { background: C.surfaceContainerLow, color: C.primary, border: 'none', boxShadow: '0 1px 3px rgba(0,0,0,0.2)' },
  }[kind];
  return (
    <button onClick={disabled ? undefined : onClick} disabled={disabled}
      style={{
        height: h, borderRadius: h / 2, padding: icon ? '0 24px 0 16px' : '0 24px',
        display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: 8,
        font: `600 ${size === 'lg' ? 16 : 14}px/1 ${C.ui}`, letterSpacing: 0.1,
        cursor: disabled ? 'default' : 'pointer', width: full ? '100%' : 'auto',
        whiteSpace: 'nowrap', transition: 'filter .15s', ...styles,
      }}
      onPointerDown={(e) => !disabled && (e.currentTarget.style.filter = 'brightness(0.93)')}
      onPointerUp={(e) => (e.currentTarget.style.filter = 'none')}
      onPointerLeave={(e) => (e.currentTarget.style.filter = 'none')}>
      {icon && <MIcon name={icon} size={18} color={styles.color} />}
      {children}
    </button>
  );
}

function M3Fab({ icon, label, onClick, C, extended }) {
  return (
    <button onClick={onClick}
      style={{
        height: 56, minWidth: 56, borderRadius: 16, border: 'none',
        background: C.primaryContainer, color: C.onPrimaryContainer,
        boxShadow: '0 3px 8px rgba(0,0,0,0.22)', cursor: 'pointer',
        display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
        gap: 10, padding: extended ? '0 20px' : 0, whiteSpace: 'nowrap',
        font: `600 15px/1 ${C.ui}`,
      }}
      onPointerDown={(e) => (e.currentTarget.style.filter = 'brightness(0.95)')}
      onPointerUp={(e) => (e.currentTarget.style.filter = 'none')}
      onPointerLeave={(e) => (e.currentTarget.style.filter = 'none')}>
      <MIcon name={icon} size={24} color={C.onPrimaryContainer} />
      {extended && label}
    </button>
  );
}

// Center-aligned / small top app bar. Sits below the status bar inset.
function TopBar({ title, subtitle, onBack, actions, C, transparent, large }) {
  return (
    <div style={{
      paddingTop: 44, flexShrink: 0,
      background: transparent ? 'transparent' : C.surface,
    }}>
      <div style={{ height: large ? 'auto' : 64, display: 'flex', alignItems: 'center', padding: '0 4px', gap: 4 }}>
        {onBack && (
          <IconBtn name="back" C={C} onClick={onBack} />
        )}
        {!large && (
          <div style={{ flex: 1, minWidth: 0, padding: onBack ? '0 4px' : '0 12px' }}>
            <div style={{ font: `500 22px/1.2 ${C.ui}`, color: C.onSurface, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{title}</div>
            {subtitle && <div style={{ font: `400 13px/1.3 ${C.ui}`, color: C.onSurfaceVariant, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>{subtitle}</div>}
          </div>
        )}
        {large && <div style={{ flex: 1 }} />}
        <div style={{ display: 'flex', alignItems: 'center', gap: 2, paddingRight: 6 }}>{actions}</div>
      </div>
      {large && (
        <div style={{ padding: '4px 28px 18px' }}>
          <div style={{ font: `400 32px/1.2 ${C.ui}`, color: C.onSurface }}>{title}</div>
        </div>
      )}
    </div>
  );
}

function IconBtn({ name, C, onClick, color, active, size = 24 }) {
  return (
    <button onClick={onClick}
      style={{
        width: 48, height: 48, borderRadius: 24, border: 'none', flexShrink: 0,
        background: active ? C.secondaryContainer : 'transparent',
        display: 'flex', alignItems: 'center', justifyContent: 'center', cursor: 'pointer',
      }}
      onPointerDown={(e) => (e.currentTarget.style.background = C.onSurface + '14')}
      onPointerUp={(e) => (e.currentTarget.style.background = active ? C.secondaryContainer : 'transparent')}
      onPointerLeave={(e) => (e.currentTarget.style.background = active ? C.secondaryContainer : 'transparent')}>
      <MIcon name={name} size={size} color={color || C.onSurfaceVariant} />
    </button>
  );
}

function Divider({ C, inset = 0 }) {
  return <div style={{ height: 1, background: C.outlineVariant, marginLeft: inset, opacity: 0.7 }} />;
}

function Chip({ label, selected, onClick, C, icon }) {
  return (
    <button onClick={onClick}
      style={{
        height: 32, borderRadius: 8, padding: '0 14px', flexShrink: 0,
        display: 'inline-flex', alignItems: 'center', gap: 6, cursor: 'pointer',
        border: selected ? 'none' : `1px solid ${C.outlineVariant}`,
        background: selected ? C.secondaryContainer : 'transparent',
        color: selected ? C.onSecondaryContainer : C.onSurfaceVariant,
        font: `500 14px/1 ${C.ui}`,
      }}>
      {selected && <MIcon name="check" size={16} color={C.onSecondaryContainer} />}
      {icon && !selected && <MIcon name={icon} size={16} color={C.onSurfaceVariant} />}
      {label}
    </button>
  );
}

// Dropdown menu anchored top-right
function Menu({ items, onClose, C }) {
  return (
    <div onClick={onClose} style={{ position: 'absolute', inset: 0, zIndex: 80 }}>
      <div onClick={(e) => e.stopPropagation()} style={{
        position: 'absolute', top: 50, right: 10, minWidth: 200,
        background: C.surfaceContainerHigh, borderRadius: 12, padding: '8px 0',
        boxShadow: '0 6px 20px rgba(0,0,0,0.28)',
      }}>
        {items.map((it, i) => (
          <button key={i} onClick={() => { onClose(); it.onClick(); }}
            style={{
              width: '100%', height: 48, padding: '0 16px', border: 'none', background: 'transparent',
              display: 'flex', alignItems: 'center', gap: 14, cursor: 'pointer',
              font: `400 16px/1 ${C.ui}`, color: it.danger ? C.error : C.onSurface, textAlign: 'left',
            }}
            onPointerDown={(e) => (e.currentTarget.style.background = C.onSurface + '14')}
            onPointerUp={(e) => (e.currentTarget.style.background = 'transparent')}
            onPointerLeave={(e) => (e.currentTarget.style.background = 'transparent')}>
            {it.icon && <MIcon name={it.icon} size={22} color={it.danger ? C.error : C.onSurfaceVariant} />}
            {it.label}
          </button>
        ))}
      </div>
    </div>
  );
}

// Bottom sheet
function Sheet({ children, onClose, C }) {
  return (
    <div onClick={onClose} style={{ position: 'absolute', inset: 0, zIndex: 90, background: 'rgba(0,0,0,0.4)', display: 'flex', alignItems: 'flex-end' }}>
      <div onClick={(e) => e.stopPropagation()} style={{
        width: '100%', background: C.surfaceContainerLow, borderRadius: '28px 28px 0 0',
        padding: '12px 0 36px', maxHeight: '82%', overflow: 'auto',
        boxShadow: '0 -8px 30px rgba(0,0,0,0.25)',
      }}>
        <div style={{ width: 32, height: 4, borderRadius: 2, background: C.onSurfaceVariant, opacity: 0.4, margin: '0 auto 14px' }} />
        {children}
      </div>
    </div>
  );
}

Object.assign(window, { M3Button, M3Fab, TopBar, IconBtn, Divider, Chip, Menu, Sheet });
